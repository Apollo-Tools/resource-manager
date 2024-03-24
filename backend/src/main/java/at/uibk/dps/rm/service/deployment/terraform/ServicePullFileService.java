package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.PrePullGroup;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.util.misc.MetricValueMapper;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Extension of the #TerraformFileService to set up the service pre pull module of service
 * deployments.
 *
 * @author matthi-g
 */
public class ServicePullFileService extends TerraformFileService {

    private final long deploymentId;

    private final List<ServiceDeployment> serviceDeployments;

    private final ConfigDTO config;

    /**
     * Create an instance from the filesystem, rootFolder, serviceDeployments and deploymentId.
     *
     * @param fileSystem the vertx file system
     * @param rootFolder the root folder of the module
     * @param serviceDeployments the list of service deployments
     * @param deploymentId the id of the deployment
     */
    public ServicePullFileService(FileSystem fileSystem, Path rootFolder, List<ServiceDeployment> serviceDeployments,
                                  long deploymentId, ConfigDTO config) {
        super(fileSystem, rootFolder, "");
        this.serviceDeployments = serviceDeployments;
        this.deploymentId = deploymentId;
        this.config = config;
    }

    @Override
    protected String getProviderString() {
        return "";
    }

    @Override
    protected String getMainFileContent() {
        return getServiceModulesString();
    }

    /**
     * Get the string that defines all pre pull modules for service deployments.
     *
     * @return the service modules string
     */
    private String getServiceModulesString() {
        HashMap<PrePullGroup, Set<String>> prePullGroups = new HashMap<>();
        StringBuilder functionsString = new StringBuilder();
        for (ServiceDeployment serviceDeployment : serviceDeployments) {
            Set<String> imageSet;
            Resource resource = serviceDeployment.getResource();
            Service service = serviceDeployment.getService();
            Map<String, MetricValue> mainMetricValues =
                MetricValueMapper.mapMetricValues(resource.getMain().getMetricValues());
            String nodeName = resource.getMain().getResourceId().equals(resource.getResourceId()) ?
                null : "\"" + resource.getName() + "\"";
            PrePullGroup pullGroup = new PrePullGroup(resource.getResourceId(), serviceDeployment.getContext(),
                serviceDeployment.getNamespace(), mainMetricValues.get("pre-pull-timeout").getValueNumber().longValue(),
                nodeName, resource.getMain().getName());
            if (!prePullGroups.containsKey(pullGroup)) {
                imageSet = new HashSet<>();
                prePullGroups.put(pullGroup, imageSet);
            } else {
                imageSet = prePullGroups.get(pullGroup);
            }
            imageSet.add("\"" + service.getImage() + "\"");
        }

        String imagePullSecrets = config.getKubeImagePullSecrets().stream()
            .map(secret -> "\"" + secret + "\"").collect(Collectors.joining(","));
        prePullGroups.forEach((prePullGroup, imageSet) -> {
            String configPath = Path.of(config.getKubeConfigDirectory(), prePullGroup.getMainResourceName())
                .toAbsolutePath().toString().replace("\\", "/");
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
                    "  image_pull_secrets = [%s]\n" +
                    "}\n", prePullGroup.getResourceId(), deploymentId, configPath, prePullGroup.getNamespace(),
                prePullGroup.getContext(), String.join(",", imageSet), prePullGroup.getTimeout(),
                prePullGroup.getNodeName(), imagePullSecrets
            ));
        });
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
