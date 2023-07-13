package at.uibk.dps.rm.service.deployment.terraform;

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

    /**
     * Create an instance from the fileSystem, rootFolder, serviceDeployment and deploymentId.
     *
     * @param fileSystem the vertx file system
     * @param rootFolder the root folder of the module
     * @param serviceDeployment the service deployment
     * @param deploymentId the id of the deployment
     */
    public ContainerDeployFileService(FileSystem fileSystem, Path rootFolder, ServiceDeployment serviceDeployment,
            long deploymentId) {
        super(fileSystem, rootFolder);
        this.rootFolder = rootFolder;
        this.serviceDeployment = serviceDeployment;
        this.deploymentId = deploymentId;
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
        Map<String, MetricValue> metricValues = MetricValueMapper.mapMetricValues(resource.getMetricValues());

        String externalIp = "";
        if (metricValues.containsKey("external-ip")) {
            externalIp = metricValues.get("external-ip").getValueString();
        }

        String configPath = Path.of(rootFolder.getParent().toString(), "config").toAbsolutePath().toString()
            .replace("\\", "/");
        String ports = service.getPorts().stream()
            .map(portEntry -> String.format("{container_port = %s, service_port = %s}", portEntry.split(":")[0],
                portEntry.split(":")[1]))
            .collect(Collectors.joining(","));
        containerString.append(String.format(
            "module \"deployment_%s\" {\n" +
            "  source = \"../../../../terraform/k8s/deployment\"\n" +
            "  config_path = \"%s\"\n" +
            "  config_context = \"%s\"\n" +
            "  namespace = \"%s\"\n" +
            "  image = \"%s\"\n" +
            "  deployment_id = %s\n" +
            "  replicas = %s\n" +
            "  cpu = \"%s\"\n" +
            "  memory = \"%sM\"\n" +
            "  ports = [%s]\n" +
            "  service_type = \"%s\"\n" +
            "  external_ip = \"%s\"\n" +
            "}\n", identifier, configPath, serviceDeployment.getContext(),
            serviceDeployment.getNamespace(), service.getImage(), deploymentId,
            service.getReplicas(), service.getCpu(), service.getMemory(), ports,
            service.getServiceType().getName(), externalIp));
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
