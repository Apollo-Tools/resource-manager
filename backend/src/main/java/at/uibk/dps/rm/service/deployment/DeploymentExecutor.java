package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.service.deployment.terraform.AWSFileService;
import at.uibk.dps.rm.service.deployment.terraform.EdgeFileService;
import at.uibk.dps.rm.service.deployment.terraform.MainFileService;
import at.uibk.dps.rm.service.deployment.terraform.FunctionFileService;
import at.uibk.dps.rm.util.DeploymentPath;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class DeploymentExecutor {

    private final Vertx vertx;

    private String edgeLoginData;

    private final DeployResourcesRequest deployRequest;

    private final DeploymentPath deploymentPath;

    public DeploymentExecutor (Vertx vertx, JsonObject jsonObject) {
        this.vertx = vertx;
        this.deployRequest = jsonObject.mapTo(DeployResourcesRequest.class);
        this.deploymentPath = new DeploymentPath(deployRequest.getReservationId());
    }

    public Single<Integer> deploy() {
        // Setup
        Set<Credentials> necessaryCredentials = new HashSet<>();
        FunctionFileService functionFileService = new FunctionFileService(vertx, deployRequest.getFunctionResources(),
            deploymentPath.getFunctionsFolder(), deployRequest.getDockerCredentials());
        List<Single<TerraformModule>> singles = setUpRegionTFDirs(necessaryCredentials);
        TerraformExecutor terraformExecutor = new TerraformExecutor(new ArrayList<>(necessaryCredentials),
            edgeLoginData);

        return
            // Build, zip functions / docker images
            functionFileService.packageCode()
            // Setup tf directories
            .flatMap(res -> Single.zip(singles, objects -> Arrays.stream(objects).map(object -> (TerraformModule) object)
                .collect(Collectors.toList())))
            .flatMapCompletable(tfModules -> {
                // TF: main files
                MainFileService mainFileService = new MainFileService(vertx.fileSystem(), deploymentPath.getRootFolder()
                    , tfModules);
                return mainFileService.setUpDirectory();
            })
            // Terraform execution
            .andThen(terraformExecutor.setPluginCacheFolder(vertx.fileSystem(), deploymentPath.getTFCacheFolder()))
            .andThen(Single.fromCallable(() -> terraformExecutor.init(deploymentPath.getRootFolder())))
            .flatMap(res -> res)
            .map(Process::exitValue)
            .flatMap(res -> terraformExecutor.apply(deploymentPath.getRootFolder()))
            .map(Process::exitValue);
    }

    private List<Single<TerraformModule>> setUpRegionTFDirs(Set<Credentials> necessaryCredentials) {
        Map<Region, List<FunctionResource>> functionResources = deployRequest.getFunctionResources()
            .stream()
            .collect(Collectors.groupingBy(functionResource -> functionResource.getResource().getRegion()));
        Map<Region, VPC> regionVPCMap = deployRequest.getVpcList()
            .stream()
            .collect(Collectors.toMap(VPC::getRegion, vpc -> vpc, (vpc1, vpc2) -> vpc1));
        List<Single<TerraformModule>> singles = new ArrayList<>();
        for (Region region: functionResources.keySet()) {
            List<FunctionResource> regionFunctionResources = functionResources.get(region);
            if (region.getName().equals("edge")) {
                // TF: Edge resources
                singles.add(edgeDeployment(regionFunctionResources));
                // Create edge login data
                composeEdgeLoginData(regionFunctionResources);
            } else {
                // TF: Cloud resources
                singles.add(cloudDeployment(region, regionFunctionResources, regionVPCMap));
                necessaryCredentials.add(deployRequest.getCredentialsList().stream()
                    .filter(credentials -> credentials.getResourceProvider().equals(region.getResourceProvider()))
                    .findFirst().get());
            }
        }
        return singles;
    }

    private void composeEdgeLoginData(List<FunctionResource> regionFunctionResources) {
        StringBuilder edgeLoginDataBuilder = new StringBuilder("edge_login_data=[");
        for (FunctionResource functionResource : regionFunctionResources) {
            Resource resource = functionResource.getResource();
            Map<String, MetricValue> metricValues = resource.getMetricValues()
                .stream()
                .collect(Collectors.toMap(metricValue -> metricValue.getMetric().getMetric(),
                    metricValue -> metricValue));
            edgeLoginDataBuilder.append("{auth_user=\\\"")
                .append(metricValues.get("openfaas-user").getValueString())
                .append("\\\",auth_pw=\\\"")
                .append(metricValues.get("openfaas-pw").getValueString())
                .append("\\\"},");
        }
        edgeLoginDataBuilder.append("]");
        edgeLoginData = edgeLoginDataBuilder.toString();
    }

    // TODO: Rework for other cloud providers
    private Single<TerraformModule> cloudDeployment(Region region, List<FunctionResource> regionFunctionResources,
                                              Map<Region, VPC> regionVPCMap) {
        // TODO: get rid of these
        String awsRole = "LabRole";
        String provider = region.getResourceProvider().getProvider();
        TerraformModule module = new TerraformModule(CloudProvider.AWS, provider + "_" +
            region.getName().replace("-", "_"));
        Path awsFolder = deploymentPath.getModuleFolder(module);
        AWSFileService fileService = new AWSFileService(vertx.fileSystem(), awsFolder, deploymentPath.getFunctionsFolder(),
            region, awsRole, regionFunctionResources, deployRequest.getReservationId(), module,
            deployRequest.getDockerCredentials().getUsername(), regionVPCMap.get(region));
        return fileService.setUpDirectory()
            .toSingle(() -> module);
    }

    protected Single<TerraformModule> edgeDeployment(List<FunctionResource> edgeFunctionResources) {
        TerraformModule module = new TerraformModule(CloudProvider.EDGE, "edge");
        Path edgeFolder = deploymentPath.getModuleFolder(module);
        EdgeFileService edgeService = new EdgeFileService(vertx.fileSystem(), edgeFolder, edgeFunctionResources,
            deployRequest.getReservationId(), deployRequest.getDockerCredentials().getUsername());
        return edgeService.setUpDirectory()
            .toSingle(() -> module);
    }

}
