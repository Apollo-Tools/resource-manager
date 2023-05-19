package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.PrePullGroup;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.util.misc.MetricValueMapper;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.util.*;

/**
 * Extension of the #TerraformFileService to set up the container pre pull module of service
 * reservation.
 *
 * @author matthi-g
 */
public class ContainerPullFileService extends TerraformFileService {

    private final long reservationId;

    private final List<ServiceReservation> serviceReservations;

    private final Path rootFolder;

    /**
     * Create an instance from the filesystem, rootFolder, serviceReservations and reservationId.
     *
     * @param fileSystem the vertx file system
     * @param rootFolder the root folder of the module
     * @param serviceReservations the list of service reservations
     * @param reservationId the id of the reservation
     */
    public ContainerPullFileService(FileSystem fileSystem, Path rootFolder, List<ServiceReservation> serviceReservations,
            long reservationId) {
        super(fileSystem, rootFolder);
        this.rootFolder = rootFolder;
        this.serviceReservations = serviceReservations;
        this.reservationId = reservationId;
    }

    @Override
    protected String getProviderString() {
        return "";
    }

    @Override
    protected String getMainFileContent() {
        return getContainerModulesString();
    }

    /**
     * Get the string that defines all pre pull modules for container deployments.
     *
     * @return the container modules string
     */
    private String getContainerModulesString() {
        HashMap<PrePullGroup, List<String>> prePullGroups = new HashMap<>();
        StringBuilder functionsString = new StringBuilder();
        for (ServiceReservation serviceReservation : serviceReservations) {
            List<String> imageList;
            Resource resource = serviceReservation.getResource();
            Service service = serviceReservation.getService();
            Map<String, MetricValue> metricValues = MetricValueMapper.mapMetricValues(resource.getMetricValues());
            PrePullGroup pullGroup = new PrePullGroup(resource.getResourceId(), serviceReservation.getContext(),
                serviceReservation.getNamespace(), metricValues.get("pre-pull-timeout").getValueNumber().longValue());
            if (!prePullGroups.containsKey(pullGroup)) {
                imageList = new ArrayList<>();
                prePullGroups.put(pullGroup, imageList);
            } else {
                imageList = prePullGroups.get(pullGroup);
            }
            imageList.add("\"" + service.getName() + "\"");
        }

        String configPath = Path.of(rootFolder.toString(), "config").toAbsolutePath().toString()
            .replace("\\", "/");
        prePullGroups.forEach((prePullGroup, imageList) ->
            functionsString.append(String.format(
                "module \"pre_pull_%s\" {\n" +
                    "  source = \"../../../terraform/k8s/prepull\"\n" +
                    "  reservation_id = %s\n" +
                    "  config_path = \"%s\"\n" +
                    "  namespace = \"%s\"\n" +
                    "  config_context = \"%s\"\n" +
                    "  images = [%s]\n" +
                    "  timeout = \"%sm\"\n" +
                    "}\n", prePullGroup.getIdentifier(), reservationId, configPath, prePullGroup.getNamespace(),
                prePullGroup.getContext(), String.join(",", imageList), prePullGroup.getTimeout()
        )));
        return functionsString.toString();
    }

    @Override
    protected String getCredentialVariablesString() {
        return "";
    }

    @Override
    protected String getVariablesFileContent() {
        return "";
    }

    @Override
    protected String getOutputString() {
        return "";
    }

    @Override
    protected String getOutputsFileContent() {
        return this.getOutputString();
    }
}
