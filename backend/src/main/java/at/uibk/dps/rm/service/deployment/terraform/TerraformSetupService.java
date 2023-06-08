package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.TerminateResourcesRequest;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.deployment.TerraformModule;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.util.misc.RegionMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.util.*;
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
            singles.add(functionDeployment(region, regionFunctionReservations, regionVPCMap));
            composeCloudLoginData(deployRequest.getCredentialsList(), region);
            composeOpenFaasLoginData(regionFunctionReservations);
        }
        if (!deployRequest.getServiceReservations().isEmpty()) {
            singles.add(containerDeployment(deployRequest.getServiceReservations()));
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
            composeCloudLoginData(terminateRequest.getCredentialsList(), region);
            composeOpenFaasLoginData(regionFunctionReservations);
        }
        return Single.just(this.credentials);
    }

    /**
     * Compose the edge login data that is necessary for deployment. It is formatted to be used as
     * command line parameter of the terraform cli.
     *
     * @param regionFunctionReservations the function reservations grouped by region
     */
    private void composeOpenFaasLoginData(List<FunctionReservation> regionFunctionReservations) {
        String escapeString = "\"";
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            escapeString = "\\\"";
        }
        Set<Long> credentialResources = new HashSet<>();
        for (FunctionReservation functionReservation : regionFunctionReservations) {
            Resource resource = functionReservation.getResource();
            if (credentialResources.contains(resource.getResourceId()) ||
                !resource.getPlatform().getPlatform().equals(PlatformEnum.OPENFAAS.getValue())) {
                continue;
            }
            Map<String, MetricValue> metricValues = resource.getMetricValues()
                .stream()
                .collect(Collectors.toMap(metricValue -> metricValue.getMetric().getMetric(),
                    metricValue -> metricValue));
            String openFaasCredentials = "r" + resource.getResourceId() + "={auth_user=" + escapeString +
                metricValues.get("openfaas-user").getValueString() + escapeString + ",auth_pw=" + escapeString +
                metricValues.get("openfaas-pw").getValueString() + escapeString + "}";
            credentials.getOpenFaasCredentials().add(openFaasCredentials);
            credentialResources.add(resource.getResourceId());
        }
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

    /**
     * Setup everything necessary for cloud deployment in the region.
     *
     * @param region the region
     * @param regionFunctionReservations the function reservations of the region
     * @param regionVPCMap all available vpc grouped by region
     * @return a Single that emits the created terraform module
     */
    private Single<TerraformModule> functionDeployment(Region region, List<FunctionReservation> regionFunctionReservations,
                                                    Map<Region, VPC> regionVPCMap) {
        String provider = region.getResourceProvider().getProvider();
        ResourceProviderEnum resourceProvider = ResourceProviderEnum.fromString(provider);
        TerraformModule module = new TerraformModule(resourceProvider, region);
        Path moduleFolder = deploymentPath.getModuleFolder(module);
        Path functionsFolder = deploymentPath.getFunctionsFolder();
        long reservationId = deployRequest.getReservation().getReservationId();
        String dockerUsername = deployRequest.getDockerCredentials().getUsername();
        RegionFaasFileService fileService = new RegionFaasFileService(vertx.fileSystem(), moduleFolder,
            functionsFolder, region, regionFunctionReservations, reservationId, module, dockerUsername,
            regionVPCMap.get(region));
        return fileService.setUpDirectory()
            .toSingle(() -> module);
    }

    private Single<TerraformModule> containerDeployment(List<ServiceReservation> serviceReservations) {
        FileSystem fileSystem = vertx.fileSystem();
        long reservationId = deployRequest.getReservation().getReservationId();
        TerraformModule module = new TerraformModule(CloudProvider.CONTAINER, "container");
        Path containerFolder = deploymentPath.getModuleFolder(module);
        Path configPath = Path.of(containerFolder.toString(), "config");
        ContainerPullFileService containerFileService = new ContainerPullFileService(fileSystem, containerFolder,
            serviceReservations, reservationId);
        List<Completable> completables = new ArrayList<>();
        for (ServiceReservation serviceReservation : serviceReservations) {
            Path deployFolder = Path.of(containerFolder.toString(),  serviceReservation.getResourceReservationId().toString());
            ContainerDeployFileService containerDeployFileService = new ContainerDeployFileService(fileSystem,
                deployFolder, serviceReservation, reservationId);
            completables.add(containerDeployFileService.setUpDirectory());
        }

        return containerFileService.setUpDirectory()
            .andThen(Completable.merge(completables))
            .andThen(Completable.defer(() -> fileSystem.writeFile(configPath.toString(),
                Buffer.buffer(deployRequest.getKubeConfig()))))
            .toSingle(() -> module);
    }
}
