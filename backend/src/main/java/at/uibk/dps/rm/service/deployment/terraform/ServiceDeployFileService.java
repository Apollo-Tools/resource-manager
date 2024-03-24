package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.util.misc.MetricValueMapper;
import io.vertx.rxjava3.core.file.FileSystem;
import org.apache.commons.lang3.NotImplementedException;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Extension of the #TerraformFileService to set up the deployment module of service
 * deployments.
 *
 * @author matthi-g
 */
public class ServiceDeployFileService extends TerraformFileService {

    private final long deploymentId;

    private final ServiceDeployment serviceDeployment;

    private final ConfigDTO config;

    /**
     * Create an instance from the fileSystem, rootFolder, serviceDeployment and deploymentId.
     *
     * @param fileSystem the vertx file system
     * @param rootFolder the root folder of the module
     * @param serviceDeployment the service deployment
     * @param deploymentId the id of the deployment
     */
    public ServiceDeployFileService(FileSystem fileSystem, Path rootFolder, ServiceDeployment serviceDeployment,
                                    long deploymentId, ConfigDTO config) {
        super(fileSystem, rootFolder, serviceDeployment.getResourceDeploymentId().toString());
        this.serviceDeployment = serviceDeployment;
        this.deploymentId = deploymentId;
        this.config = config;
    }

    @Override
    protected String getProviderString() {
        throw new NotImplementedException();
    }

    @Override
    protected String getMainFileContent() {
        return getServiceModuleString();
    }

    /**
     * Get the string that defines the service deployments from the terraform module.
     *
     * @return the service module string
     */
    private String getServiceModuleString() {
        StringBuilder moduleString = new StringBuilder();
        Resource resource = serviceDeployment.getResource();
        Service service = serviceDeployment.getService();
        String identifier = serviceDeployment.getResourceDeploymentId().toString();
        Map<String, MetricValue> mainMetricValues =
            MetricValueMapper.mapMetricValues(resource.getMain().getMetricValues());
        String externalIp = "";
        if (mainMetricValues.containsKey("external-ip")) {
            externalIp = mainMetricValues.get("external-ip").getValueString().strip();
        }
        String configPath = Path.of(config.getKubeConfigDirectory(), resource.getMain().getName())
            .toAbsolutePath().toString().replace("\\", "/");
        String ports = service.getPorts().stream()
            .map(portEntry -> String.format("{container_port = %s, service_port = %s}", portEntry.split(":")[0],
                portEntry.split(":")[1]))
            .collect(Collectors.joining(","));
        String nodeName = resource.getMain().getResourceId().equals(resource.getResourceId()) ?
            "null" : "\"" + resource.getName() + "\"";
        String imagePullSecrets = config.getKubeImagePullSecrets().stream()
            .map(secret -> "\"" + secret + "\"").collect(Collectors.joining(","));
        String volumeMounts = service.getVolumeMounts().stream()
                .map(volumeMount -> "{name:\"" + volumeMount.getName() +
                        "\",mountPath:\"" + volumeMount.getMountPath() +
                        "\",sizeMegaBytes:" + volumeMount.getSizeMegabytes() + "}")
                .collect(Collectors.joining(","));
        String envVars = service.getEnvVars().stream()
                .map(envVar -> "{name:\"" + envVar.getName() +
                        "\",value:\"" + envVar.getValue() + "\"}")
                .collect(Collectors.joining(","));
        moduleString.append(String.format(
            "module \"deployment_%s\" {\n" +
            "  source = \"../../../terraform/k8s/deployment\"\n" +
            "  config_path = \"%s\"\n" +
            "  config_context = \"%s\"\n" +
            "  namespace = \"%s\"\n" +
            "  name = \"%s\"\n" +
            "  image = \"%s\"\n" +
            "  deployment_id = %s\n" +
            "  resource_deployment_id = %s\n" +
            "  service_id = %s\n" +
            "  replicas = %s\n" +
            "  cpu = \"%s\"\n" +
            "  memory = \"%sM\"\n" +
            "  ports = [%s]\n" +
            "  service_type = \"%s\"\n" +
            "  external_ip = \"%s\"\n" +
            "  hostname = %s\n" +
            "  image_pull_secrets = [%s]\n" +
            "  volume_mounts = [%s]\n" +
            "  env_vars = [%s]\n" +
            "}\n", identifier, configPath, serviceDeployment.getContext(),
            serviceDeployment.getNamespace(), service.getName(), service.getImage(), deploymentId,
            serviceDeployment.getResourceDeploymentId(), service.getServiceId(), service.getReplicas(),
            service.getCpu(), service.getMemory(), ports, service.getK8sServiceType().getName(), externalIp, nodeName,
            imagePullSecrets, volumeMounts, envVars));
        return moduleString.toString();
    }

    @Override
    protected String getVariablesFileContent() {
        return "";
    }

    @Override
    protected String getOutputsFileContent() {
        long identifier = serviceDeployment.getResourceDeploymentId();
        return String.format(
            "output \"service_deployment_%s\" {\n" +
            "  value = {\n" +
            "    service: module.deployment_%s.service_info\n" +
            "    pods: module.deployment_%s.pods_info\n" +
            "  }\n" +
            "}", identifier, identifier, identifier
        );
    }
}
