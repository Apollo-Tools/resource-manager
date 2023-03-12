package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.service.deployment.terraform.AWSFileService;
import at.uibk.dps.rm.service.deployment.terraform.EdgeFileService;
import at.uibk.dps.rm.service.deployment.terraform.MainFileService;
import at.uibk.dps.rm.service.deployment.terraform.FunctionFileService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.file.FileSystem;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class DeploymentExecutor {
    public Single<Integer> deploy(FileSystem fileSystem, JsonObject jsonObject) {
        DeployResourcesRequest deployResourcesRequest = jsonObject.mapTo(DeployResourcesRequest.class);
        Map<Region, List<FunctionResource>> functionResources = deployResourcesRequest.getFunctionResources()
            .stream()
            .collect(Collectors.groupingBy(functionResource -> functionResource.getResource().getRegion()));
        Map<Region, VPC> regionVPCMap = deployResourcesRequest.getVpcList()
            .stream()
            .collect(Collectors.toMap(VPC::getRegion, vpc -> vpc, (vpc1, vpc2) -> vpc1));
        Single<Integer> result = Single.just(-1);
        try {
            Path rootFolder = Paths.get("temp\\reservation_" + deployResourcesRequest.getReservationId());
            Path functionsDir = Path.of(rootFolder.toString(), "functions");
            // TF: create all deployment files
            StringBuilder edgeLoginData = new StringBuilder();
            Set<Credentials> necessaryCredentials = new HashSet<>();
            List<Single<TerraformModule>> singles = new ArrayList<>();
            for (Region region: functionResources.keySet()) {
                List<FunctionResource> regionFunctionResources = functionResources.get(region);
                if (region.getName().equals("edge")) {
                    // TF: Edge resources
                    singles.add(edgeDeployment(fileSystem, deployResourcesRequest, regionFunctionResources, rootFolder));
                    // Create edge login data
                    edgeLoginData.append("edge_login_data=[");
                    for (FunctionResource functionResource : regionFunctionResources) {
                        Resource resource = functionResource.getResource();
                        Map<String, MetricValue> metricValues = resource.getMetricValues()
                            .stream()
                            .collect(Collectors.toMap(metricValue -> metricValue.getMetric().getMetric(),
                                metricValue -> metricValue));
                        edgeLoginData.append("{auth_user=\\\"")
                            .append(metricValues.get("openfaas-user").getValueString())
                            .append("\\\",auth_pw=\\\"")
                            .append(metricValues.get("openfaas-pw").getValueString())
                            .append("\\\"},");
                    }
                    edgeLoginData.append("]");
                } else {
                    // TF: Cloud resources
                    singles.add(cloudDeployment(fileSystem, deployResourcesRequest, rootFolder, functionsDir,
                        region, regionFunctionResources, regionVPCMap));
                    necessaryCredentials.add(deployResourcesRequest.getCredentialsList().stream()
                        .filter(credentials -> credentials.getResourceProvider().equals(region.getResourceProvider()))
                        .findFirst().get());
                }
            }
            // Build functions
            FunctionFileService functionFileService = new FunctionFileService(deployResourcesRequest.getFunctionResources(),
                functionsDir, deployResourcesRequest.getDockerCredentials());
            //result = result.flatMap(prevRes -> functionFileService.packageCode());
            // Run terraform
            // TODO: make non blocking
            TerraformExecutor terraformExecutor = new TerraformExecutor(new ArrayList<>(necessaryCredentials),
                edgeLoginData.toString());
            terraformExecutor.setPluginCacheFolder(Paths.get("temp\\plugin_cache").toAbsolutePath());
            result =
                Single.zip(singles, objects -> Arrays.stream(objects).map(object -> (TerraformModule) object)
                    .collect(Collectors.toList()))
                .flatMapCompletable(tfModules -> {
                    // TF: main files
                    MainFileService mainFileService = new MainFileService(fileSystem, rootFolder, tfModules);
                    return mainFileService.setUpDirectory();
                })
                .andThen(functionFileService.packageCode())
                .flatMap(prevRes -> terraformExecutor.init(rootFolder))
                .map(Process::exitValue)
                .flatMap(res -> terraformExecutor.apply(rootFolder))
                .map(Process::exitValue)
                    .doOnError(Throwable::printStackTrace);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    // TODO: Rework for other cloud providers
    protected Single<TerraformModule> cloudDeployment(FileSystem fileSystem, DeployResourcesRequest deployResourcesRequest, Path rootFolder,
                                              Path functionsDir, Region region, List<FunctionResource> functionResources,
                                              Map<Region, VPC> regionVPCMap)
        throws IOException {
        // TODO: get rid of these
        String awsRole = "LabRole";
        String provider = region.getResourceProvider().getProvider();
        TerraformModule module = new TerraformModule(CloudProvider.AWS, provider + "_" +
            region.getName().replace("-", "_"));
        Path awsFolder = Paths.get(rootFolder + "\\" + module.getModuleName());
        AWSFileService fileService = new AWSFileService(fileSystem, awsFolder, functionsDir, region,
            awsRole, functionResources, deployResourcesRequest.getReservationId(), module,
            deployResourcesRequest.getDockerCredentials().getUsername(), regionVPCMap.get(region));
        return fileService.setUpDirectory()
            .toSingle(() -> module);
    }

    protected Single<TerraformModule> edgeDeployment(FileSystem fileSystem, DeployResourcesRequest deployResourcesRequest,
                                             List<FunctionResource> functionResources, Path rootFolder) throws IOException {
        TerraformModule module = new TerraformModule(CloudProvider.EDGE, "edge");
        Path edgeFolder = Paths.get(rootFolder.toString(), module.getModuleName());
        EdgeFileService edgeService = new EdgeFileService(fileSystem, edgeFolder, functionResources,
            deployResourcesRequest.getReservationId(), deployResourcesRequest.getDockerCredentials().getUsername());
        return edgeService.setUpDirectory()
            .toSingle(() -> module);
    }

}
