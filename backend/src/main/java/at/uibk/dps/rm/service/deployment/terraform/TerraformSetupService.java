package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.deployment.TerraformModule;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import lombok.AllArgsConstructor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TerraformSetupService {

    private final Vertx vertx;

    private final DeployResourcesRequest deployRequest;

    private final DeploymentPath deploymentPath;

    private final DeploymentCredentials credentials;

    public List<Single<TerraformModule>> setUpTFModuleDirs() {
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
                credentials.getCloudCredentials().add(deployRequest.getCredentialsList().stream()
                    .filter(credentials -> credentials.getResourceProvider().equals(region.getResourceProvider()))
                    .findFirst().get());
            }
        }
        return singles;
    }

    private void composeEdgeLoginData(List<FunctionResource> regionFunctionResources) {
        credentials.getEdgeLoginCredentials().setLength(0);
        credentials.getEdgeLoginCredentials().append("edge_login_data=[");
        for (FunctionResource functionResource : regionFunctionResources) {
            Resource resource = functionResource.getResource();
            Map<String, MetricValue> metricValues = resource.getMetricValues()
                .stream()
                .collect(Collectors.toMap(metricValue -> metricValue.getMetric().getMetric(),
                    metricValue -> metricValue));
            credentials.getEdgeLoginCredentials().append("{auth_user=\\\"")
                .append(metricValues.get("openfaas-user").getValueString())
                .append("\\\",auth_pw=\\\"")
                .append(metricValues.get("openfaas-pw").getValueString())
                .append("\\\"},");
        }
        credentials.getEdgeLoginCredentials().append("]");
    }

    //TODO: Rework for other cloud providers
    private Single<TerraformModule> cloudDeployment(Region region, List<FunctionResource> regionFunctionResources,
                                                    Map<Region, VPC> regionVPCMap) {
        //TODO: get rid of these
        String awsRole = "LabRole";
        String provider = region.getResourceProvider().getProvider();
        TerraformModule module = new TerraformModule(CloudProvider.AWS, provider + "_" +
            region.getName().replace("-", "_"));
        Path awsFolder = deploymentPath.getModuleFolder(module);
        AWSFileService fileService = new AWSFileService(vertx.fileSystem(), awsFolder, deploymentPath.getFunctionsFolder(),
            region, awsRole, regionFunctionResources, deployRequest.getReservation().getReservationId(), module,
            deployRequest.getDockerCredentials().getUsername(), regionVPCMap.get(region));
        return fileService.setUpDirectory()
            .toSingle(() -> module);
    }

    private Single<TerraformModule> edgeDeployment(List<FunctionResource> edgeFunctionResources) {
        TerraformModule module = new TerraformModule(CloudProvider.EDGE, "edge");
        Path edgeFolder = deploymentPath.getModuleFolder(module);
        EdgeFileService edgeService = new EdgeFileService(vertx.fileSystem(), edgeFolder, edgeFunctionResources,
            deployRequest.getReservation().getReservationId(), deployRequest.getDockerCredentials().getUsername());
        return edgeService.setUpDirectory()
            .toSingle(() -> module);
    }

}
