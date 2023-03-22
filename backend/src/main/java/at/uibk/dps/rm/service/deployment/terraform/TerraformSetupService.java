package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.TerminateResourcesRequest;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.deployment.TerraformModule;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.util.RegionMapper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TerraformSetupService {

    private final Vertx vertx;

    private DeployResourcesRequest deployRequest;

    private TerminateResourcesRequest terminateRequest;

    private final DeploymentPath deploymentPath;

    private final DeploymentCredentials credentials;

    public TerraformSetupService(Vertx vertx, DeployResourcesRequest deployRequest, DeploymentPath deploymentPath,
                                 DeploymentCredentials credentials) {
        this.vertx = vertx;
        this.deployRequest = deployRequest;
        this.deploymentPath = deploymentPath;
        this.credentials = credentials;
    }

    public TerraformSetupService(Vertx vertx, TerminateResourcesRequest terminateRequest, DeploymentPath deploymentPath,
                                 DeploymentCredentials credentials) {
        this.vertx = vertx;
        this.terminateRequest = terminateRequest;
        this.deploymentPath = deploymentPath;
        this.credentials = credentials;
    }

    public List<Single<TerraformModule>> setUpTFModuleDirs() {
        if (deployRequest == null) {
            throw new IllegalStateException("deployRequest must not be null");
        }
        Map<Region, List<FunctionResource>> functionResources = RegionMapper
            .mapFunctionResources(deployRequest.getFunctionResources());
        Map<Region, VPC> regionVPCMap = RegionMapper.mapVPCs(deployRequest.getVpcList());
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
                composeCloudLoginData(deployRequest.getCredentialsList(), region);
            }
        }
        return singles;
    }

    public DeploymentCredentials getDeploymentCredentials() {
        if (terminateRequest == null) {
            throw new IllegalStateException("terminateRequest must not be null");
        }
        Map<Region, List<FunctionResource>> functionResources = RegionMapper
            .mapFunctionResources(terminateRequest.getFunctionResources());
        for (Region region: functionResources.keySet()) {
            List<FunctionResource> regionFunctionResources = functionResources.get(region);
            if (region.getName().equals("edge")) {
                // Get edge login data
                composeEdgeLoginData(regionFunctionResources);
            } else {
                composeCloudLoginData(terminateRequest.getCredentialsList(), region);
            }
        }
        return this.credentials;
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

    private void composeCloudLoginData(List<Credentials> credentialsList, Region region) {
        credentialsList.stream()
            .filter(filterCredentials -> filterCredentials.getResourceProvider().equals(region.getResourceProvider()))
            .findFirst()
            .ifPresent(foundCredentials -> credentials.getCloudCredentials().add(foundCredentials));
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
