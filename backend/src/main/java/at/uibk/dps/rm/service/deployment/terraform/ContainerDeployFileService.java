package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.util.misc.MetricValueMapper;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Extension of the #TerraformFileService to set up the container deployment module of service
 * deployments.
 *
 * @author matthi-g
 */
public class ContainerDeployFileService extends TerraformFileService {

    private final long deploymentId;

    private final ServiceDeployment serviceDeployment;

    private final Path rootFolder;

    private final ConfigDTO config;

    /**
     * Create an instance from the fileSystem, rootFolder, serviceDeployment and deploymentId.
     *
     * @param fileSystem the vertx file system
     * @param rootFolder the root folder of the module
     * @param serviceDeployment the service deployment
     * @param deploymentId the id of the deployment
     */
    public ContainerDeployFileService(FileSystem fileSystem, Path rootFolder, ServiceDeployment serviceDeployment,
            long deploymentId, ConfigDTO config) {
        super(fileSystem, rootFolder);
        this.rootFolder = rootFolder;
        this.serviceDeployment = serviceDeployment;
        this.deploymentId = deploymentId;
        this.config = config;
    }

    @Override
    protected String getProviderString() {
        return "terraform {\n" +
            "  required_providers {\n" +
            "    kubernetes = {\n" +
            "      source = \"hashicorp/kubernetes\"\n" +
            "      version = \"2.20.0\"\n" +
            "    }\n" +
            "  }\n" +
            "  required_version = \">= 1.2.0\"\n" +
            "}\n";
    }

    @Override
    protected String getMainFileContent() {
        return getProviderString() + getContainerModulesString();
    }

    /**
     * Get the string that defines the container deployment from the terraform module.
     *
     * @return the container modules string
     */
    private String getContainerModulesString() {
        StringBuilder containerString = new StringBuilder();
        Resource resource = serviceDeployment.getResource();
        Service service = serviceDeployment.getService();
        String identifier = resource.getResourceId() + "_" + service.getServiceId();
        Map<String, MetricValue> mainMetricValues =
            MetricValueMapper.mapMetricValues(resource.getMain().getMetricValues());
        Map<String, MetricValue> metricValues = MetricValueMapper.mapMetricValues(resource.getMetricValues());

        String externalIp = "";
        if (mainMetricValues.containsKey("external-ip")) {
            externalIp = mainMetricValues.get("external-ip").getValueString();
        }

        String configPath = Path.of(rootFolder.getParent().toString(), "config").toAbsolutePath().toString()
            .replace("\\", "/");
        String ports = service.getPorts().stream()
            .map(portEntry -> String.format("{container_port = %s, service_port = %s}", portEntry.split(":")[0],
                portEntry.split(":")[1]))
            .collect(Collectors.joining(","));
        String hostname = metricValues.containsKey("hostname") ?
            "\"" + metricValues.get("hostname").getValueString() + "\"" : "null";
        String imagePullSecrets = config.getKubeImagePullSecrets().stream()
            .map(secret -> "\"" + secret + "\"").collect(Collectors.joining(","));
        containerString.append(String.format(
            "module \"deployment_%s\" {\n" +
            "  source = \"../../../../terraform/k8s/deployment\"\n" +
            "  config_path = \"%s\"\n" +
            "  config_context = \"%s\"\n" +
            "  namespace = \"%s\"\n" +
            "  name = \"%s\"\n" +
            "  image = \"%s\"\n" +
            "  deployment_id = %s\n" +
            "  replicas = %s\n" +
            "  cpu = \"%s\"\n" +
            "  memory = \"%sM\"\n" +
            "  ports = [%s]\n" +
            "  service_type = \"%s\"\n" +
            "  external_ip = \"%s\"\n" +
            "  hostname = %s\n" +
            "  image_pull_secrets = [%s]\n" +
            "}\n", identifier, configPath, serviceDeployment.getContext(),
            serviceDeployment.getNamespace(), service.getName(), service.getImage(), deploymentId,
            service.getReplicas(), service.getCpu(), service.getMemory(), ports,
            service.getServiceType().getName(), externalIp, hostname, imagePullSecrets));
        return containerString.toString();
    }

    @Override
    protected String getVariablesFileContent() {
        return "";
    }

    @Override
    protected String getOutputsFileContent() {
        return "";
    }
}
