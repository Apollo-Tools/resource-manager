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
 * reservation.
 *
 * @author matthi-g
 */
public class ContainerDeployFileService extends TerraformFileService {

    private final long reservationId;

    private final ServiceDeployment serviceReservation;

    private final Path rootFolder;

    /**
     * Create an instance from the fileSystem, rootFolder, serviceReservation and reservationId.
     *
     * @param fileSystem the vertx file system
     * @param rootFolder the root folder of the module
     * @param serviceReservation the service reservation
     * @param reservationId the id of the reservation
     */
    public ContainerDeployFileService(FileSystem fileSystem, Path rootFolder, ServiceDeployment serviceReservation,
            long reservationId) {
        super(fileSystem, rootFolder);
        this.rootFolder = rootFolder;
        this.serviceReservation = serviceReservation;
        this.reservationId = reservationId;
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
        Resource resource = serviceReservation.getResource();
        Service service = serviceReservation.getService();
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
            "  reservation_id = %s\n" +
            "  replicas = %s\n" +
            "  cpu = \"%s\"\n" +
            "  memory = \"%sM\"\n" +
            "  ports = [%s]\n" +
            "  service_type = \"%s\"\n" +
            "  external_ip = \"%s\"\n" +
            "}\n", identifier, configPath, serviceReservation.getContext(),
            serviceReservation.getNamespace(), service.getImage(), reservationId,
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
