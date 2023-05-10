package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.TerminateResourcesRequest;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.deployment.TerraformModule;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.util.misc.RegionMapper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This service is used to setup everything related to the deployment with terraform. This includes
 * creation of the necessary directories and files, composing and packaging the source code of
 * functions and building and pushing docker images.
 *
 * @author matthi-g
 */
public class TerraformSetupService {

    private final Vertx vertx;

    private DeployResourcesRequest deployRequest;

    private TerminateResourcesRequest terminateRequest;

    private final DeploymentPath deploymentPath;

    private final DeploymentCredentials credentials;

    /**
     * Create an instance from vertx, deployRequest, deploymentPath and the credentials.
     *
     * @param vertx the vertx instance
     * @param deployRequest the request which contains all data necessary for deployment
     * @param deploymentPath the deployment path
     * @param credentials the deployment credentials
     */
    public TerraformSetupService(Vertx vertx, DeployResourcesRequest deployRequest, DeploymentPath deploymentPath,
                                 DeploymentCredentials credentials) {
        this.vertx = vertx;
        this.deployRequest = deployRequest;
        this.deploymentPath = deploymentPath;
        this.credentials = credentials;
    }

    /**
     * Create an instance from vertx, terminate, deploymentPath and the credentials.
     *
     * @param vertx the vertx instance
     * @param terminateRequest the request which contains all data necessary for termination
     * @param deploymentPath the deployment path
     * @param credentials the deployment credentials
     */
    public TerraformSetupService(Vertx vertx, TerminateResourcesRequest terminateRequest, DeploymentPath deploymentPath,
                                 DeploymentCredentials credentials) {
        this.vertx = vertx;
        this.terminateRequest = terminateRequest;
        this.deploymentPath = deploymentPath;
        this.credentials = credentials;
    }

    /**
     * Create all terraform module directories.
     *
     * @return a Single that emits a list of all terraform modules from the deployment
     */
    public Single<List<TerraformModule>> setUpTFModuleDirs() {
        if (deployRequest == null) {
            return Single.error(new IllegalStateException("deployRequest must not be null"));
        }
        Map<Region, List<FunctionReservation>> functionReservations = RegionMapper
            .mapFunctionReservations(deployRequest.getFunctionReservations());
        Map<Region, VPC> regionVPCMap = RegionMapper.mapVPCs(deployRequest.getVpcList());
        List<Single<TerraformModule>> singles = new ArrayList<>();
        for (Region region: functionReservations.keySet()) {
            List<FunctionReservation> regionFunctionReservations = functionReservations.get(region);
            if (region.getName().equals("edge")) {
                // TF: Edge resources
                singles.add(edgeDeployment(regionFunctionReservations));
                // Create edge login data
                composeEdgeLoginData(regionFunctionReservations);
            } else {
                // TF: Cloud resources
                singles.add(cloudDeployment(region, regionFunctionReservations, regionVPCMap));
                composeCloudLoginData(deployRequest.getCredentialsList(), region);
            }
        }

        return Single.zip(singles, objects -> Arrays.stream(objects).map(object -> (TerraformModule) object)
            .collect(Collectors.toList()));
    }

    /**
     * Get the credentials that are necessary for termination.
     *
     * @return a Single that emits the credentials
     */
    public Single<DeploymentCredentials> getTerminationCredentials() {
        if (terminateRequest == null) {
            return Single.error(new IllegalStateException("terminateRequest must not be null"));
        }
        Map<Region, List<FunctionReservation>> functionReservations = RegionMapper
            .mapFunctionReservations(terminateRequest.getFunctionReservations());
        for (Region region: functionReservations.keySet()) {
            List<FunctionReservation> regionFunctionReservations = functionReservations.get(region);
            if (region.getName().equals("edge")) {
                // Get edge login data
                composeEdgeLoginData(regionFunctionReservations);
            } else {
                composeCloudLoginData(terminateRequest.getCredentialsList(), region);
            }
        }
        return Single.just(this.credentials);
    }

    /**
     * Compose the edge login data that is necessary for deployment. It is formatted to be used as
     * command line parameter of the terraform cli.
     *
     * @param regionFunctionReservations the function reservations grouped by region
     */
    private void composeEdgeLoginData(List<FunctionReservation> regionFunctionReservations) {
        StringBuilder edgeCredentials = new StringBuilder();
        edgeCredentials.append("edge_login_data=[");
        String escapeString = "\"";
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            escapeString = "\\\"";
        }
        for (FunctionReservation functionReservation : regionFunctionReservations) {
            Resource resource = functionReservation.getResource();
            Map<String, MetricValue> metricValues = resource.getMetricValues()
                .stream()
                .collect(Collectors.toMap(metricValue -> metricValue.getMetric().getMetric(),
                    metricValue -> metricValue));
            edgeCredentials.append("{auth_user=").append(escapeString)
                .append(metricValues.get("openfaas-user").getValueString())
                .append(escapeString).append(",auth_pw=").append(escapeString)
                .append(metricValues.get("openfaas-pw").getValueString())
                .append(escapeString)
                .append("},");
        }
        edgeCredentials.append("]");
        credentials.setEdgeLoginCredentials(edgeCredentials.toString());
    }

    /**
     * Compose the cloud login data that is necessary for deployment and termination.
     *
     * @param credentialsList the list that contains all credentials
     * @param region the region
     */
    private void composeCloudLoginData(List<Credentials> credentialsList, Region region) {
        credentialsList.stream()
            .filter(filterCredentials -> filterCredentials.getResourceProvider().equals(region.getResourceProvider()))
            .findFirst()
            .ifPresent(foundCredentials -> credentials.getCloudCredentials().add(foundCredentials));
    }

    //TODO: Rework for other cloud providers

    /**
     * Setup everything necessary for cloud deployment in the region.
     *
     * @param region the region
     * @param regionFunctionReservations the function reservations of the region
     * @param regionVPCMap all available vpc grouped by region
     * @return a Single that emits the created terraform module
     */
    private Single<TerraformModule> cloudDeployment(Region region, List<FunctionReservation> regionFunctionReservations,
                                                    Map<Region, VPC> regionVPCMap) {
        //TODO: get rid of hard coded labRole
        String awsRole = "LabRole";
        String provider = region.getResourceProvider().getProvider();
        TerraformModule module = new TerraformModule(CloudProvider.AWS, provider + "_" +
            region.getName().replace("-", "_"));
        Path awsFolder = deploymentPath.getModuleFolder(module);
        AWSFileService fileService = new AWSFileService(vertx.fileSystem(), awsFolder, deploymentPath.getFunctionsFolder(),
            region, awsRole, regionFunctionReservations, deployRequest.getReservation().getReservationId(), module,
            deployRequest.getDockerCredentials().getUsername(), regionVPCMap.get(region));
        return fileService.setUpDirectory()
            .toSingle(() -> module);
    }

    /**
     * Setup everything necessary for edge deployment.
     *
     * @param edgeFunctionReservations the function reservations for edge deployment
     * @return a Single that emits the created terraform module
     */
    private Single<TerraformModule> edgeDeployment(List<FunctionReservation> edgeFunctionReservations) {
        TerraformModule module = new TerraformModule(CloudProvider.EDGE, "edge");
        Path edgeFolder = deploymentPath.getModuleFolder(module);
        EdgeFileService edgeService = new EdgeFileService(vertx.fileSystem(), edgeFolder, edgeFunctionReservations,
            deployRequest.getReservation().getReservationId(), deployRequest.getDockerCredentials().getUsername());
        return edgeService.setUpDirectory()
            .toSingle(() -> module);
    }
}
