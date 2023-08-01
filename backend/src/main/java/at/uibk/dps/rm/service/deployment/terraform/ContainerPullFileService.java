package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.PrePullGroup;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.util.misc.MetricValueMapper;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.util.*;

/**
 * Extension of the #TerraformFileService to set up the container pre pull module of service
 * deployments.
 *
 * @author matthi-g
 */
public class ContainerPullFileService extends TerraformFileService {

    private final long deploymentId;

    private final List<ServiceDeployment> serviceDeployments;

    private final Path rootFolder;

    /**
     * Create an instance from the filesystem, rootFolder, serviceDeployments and deploymentId.
     *
     * @param fileSystem the vertx file system
     * @param rootFolder the root folder of the module
     * @param serviceDeployments the list of service deployments
     * @param deploymentId the id of the deployment
     */
    public ContainerPullFileService(FileSystem fileSystem, Path rootFolder, List<ServiceDeployment> serviceDeployments,
            long deploymentId) {
        super(fileSystem, rootFolder);
        this.rootFolder = rootFolder;
        this.serviceDeployments = serviceDeployments;
        this.deploymentId = deploymentId;
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
        for (ServiceDeployment serviceDeployment : serviceDeployments) {
            List<String> imageList;
            Resource resource = serviceDeployment.getResource();
            Service service = serviceDeployment.getService();
            Map<String, MetricValue> mainMetricValues =
                MetricValueMapper.mapMetricValues(resource.getMain().getMetricValues());
            Map<String, MetricValue> metricValues = MetricValueMapper.mapMetricValues(resource.getMetricValues());
            String hostname = metricValues.containsKey("hostname") ?
                "\"" + metricValues.get("hostname").getValueString() + "\"" : null;
            PrePullGroup pullGroup = new PrePullGroup(resource.getResourceId(), serviceDeployment.getContext(),
                serviceDeployment.getNamespace(), mainMetricValues.get("pre-pull-timeout").getValueNumber().longValue(),
                hostname);
            if (!prePullGroups.containsKey(pullGroup)) {
                imageList = new ArrayList<>();
                prePullGroups.put(pullGroup, imageList);
            } else {
                imageList = prePullGroups.get(pullGroup);
            }
            imageList.add("\"" + service.getImage() + "\"");
        }

        String configPath = Path.of(rootFolder.toString(), "config").toAbsolutePath().toString()
            .replace("\\", "/");
        prePullGroups.forEach((prePullGroup, imageList) ->
            functionsString.append(String.format(
                "module \"pre_pull_%s\" {\n" +
                    "  source = \"../../../terraform/k8s/prepull\"\n" +
                    "  deployment_id = %s\n" +
                    "  config_path = \"%s\"\n" +
                    "  namespace = \"%s\"\n" +
                    "  config_context = \"%s\"\n" +
                    "  images = [%s]\n" +
                    "  timeout = \"%sm\"\n" +
                    "  hostname = %s\n" +
                    "}\n", prePullGroup.getIdentifier(), deploymentId, configPath, prePullGroup.getNamespace(),
                prePullGroup.getContext(), String.join(",", imageList), prePullGroup.getTimeout(),
                prePullGroup.getHostname()
        )));
        return functionsString.toString();
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
