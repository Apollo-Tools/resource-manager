package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.module.*;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeployTerminateDTO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDTO;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.util.misc.RegionMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
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

    private DeployResourcesDTO deployRequest;

    private DeployTerminateDTO terminateRequest;

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
    public TerraformSetupService(Vertx vertx, DeployResourcesDTO deployRequest, DeploymentPath deploymentPath,
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
    public TerraformSetupService(Vertx vertx, TerminateResourcesDTO terminateRequest, DeploymentPath deploymentPath,
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
    public Single<List<TerraformModule>> setUpTFModuleDirs(ConfigDTO config) {
        if (deployRequest == null) {
            return Single.error(new IllegalStateException("deployRequest must not be null"));
        }
        Map<Region, List<FunctionDeployment>> functionDeployments = RegionMapper
            .mapFunctionDeployments(deployRequest.getFunctionDeployments());
        Map<Region, VPC> regionVPCMap = RegionMapper.mapVPCs(deployRequest.getVpcList());
        List<Single<TerraformModule>> singles = new ArrayList<>();
        for (Region region: functionDeployments.keySet()) {
            List<FunctionDeployment> regionFunctionDeployments = functionDeployments.get(region);
            singles.add(functionDeployment(region, regionFunctionDeployments, regionVPCMap));
            composeCloudLoginData(deployRequest.getCredentialsList(), region);
            composeOpenFaasLoginData(regionFunctionDeployments);
        }
        if (!deployRequest.getServiceDeployments().isEmpty()) {
            singles.add(servicePrePull(deployRequest.getServiceDeployments(), config));
            singles.add(serviceStartupTermination(deployRequest.getServiceDeployments(), config));
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
        Map<Region, List<FunctionDeployment>> functionDeployments = RegionMapper
            .mapFunctionDeployments(terminateRequest.getFunctionDeployments());
        for (Region region: functionDeployments.keySet()) {
            List<FunctionDeployment> regionFunctionDeployments = functionDeployments.get(region);
            composeCloudLoginData(terminateRequest.getCredentialsList(), region);
            composeOpenFaasLoginData(regionFunctionDeployments);
        }
        return Single.just(this.credentials);
    }

    /**
     * Compose the OpenFaaS login data that is necessary for deployment. It is formatted to be used
     * as command line parameters of the terraform cli.
     *
     * @param regionFunctionDeployments the function deployments grouped by region
     */
    private void composeOpenFaasLoginData(List<FunctionDeployment> regionFunctionDeployments) {
        String escapeString = "\"";
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            escapeString = "\\\"";
        }
        Set<Long> credentialResources = new HashSet<>();
        for (FunctionDeployment functionDeployment : regionFunctionDeployments) {
            Resource resource = functionDeployment.getResource();
            if (credentialResources.contains(resource.getResourceId()) ||
                !resource.getMain().getPlatform().getPlatform().equals(PlatformEnum.OPENFAAS.getValue())) {
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
     * Setup everything necessary for deployment in the region.
     *
     * @param region the region
     * @param regionFunctionDeployments the function deployments of the region
     * @param regionVPCMap all available vpc grouped by region
     * @return a Single that emits the created terraform module
     */
    private Single<TerraformModule> functionDeployment(Region region,
            List<FunctionDeployment> regionFunctionDeployments, Map<Region, VPC> regionVPCMap) {
        String provider = region.getResourceProvider().getProvider();
        ResourceProviderEnum resourceProvider = ResourceProviderEnum.fromString(provider);
        FaasModule module = new FaasModule(resourceProvider, region);
        long deploymentId = deployRequest.getDeployment().getDeploymentId();
        DockerCredentials dockerCredentials = deployRequest.getDeploymentCredentials().getDockerCredentials();
        RegionFaasFileService fileService = new RegionFaasFileService(vertx.fileSystem(), deploymentPath, region,
            regionFunctionDeployments, deploymentId, module, dockerCredentials, regionVPCMap.get(region));
        return fileService.setUpDirectory()
            .toSingle(() -> module);
    }

    /**
     * Setup everything necessary for pre pulling of container images of service deployments.
     *
     * @param serviceDeployments the service deployments
     * @return a Single that emits the created terraform module
     */
    private Single<TerraformModule> servicePrePull(List<ServiceDeployment> serviceDeployments,
                                                   ConfigDTO config) {
        FileSystem fileSystem = vertx.fileSystem();
        long deploymentId = deployRequest.getDeployment().getDeploymentId();
        TerraformModule prepullModule = new ServiceModule("service_prepull",
            ModuleType.SERVICE_PREPULL);
        Path prepullDir = deploymentPath.getModuleFolder(prepullModule);
        ServicePullFileService servicePullFileService = new ServicePullFileService(fileSystem, prepullDir,
            serviceDeployments, deploymentId, config);
        return servicePullFileService.setUpDirectory()
            .toSingle(() -> prepullModule);
    }

    /**
     * Setup everything necessary for the startup/termination of service deployments.
     *
     * @param serviceDeployments the service deployments
     * @return a Single that emits the created terraform module
     */
    private Single<TerraformModule> serviceStartupTermination(List<ServiceDeployment> serviceDeployments,
            ConfigDTO config) {
        FileSystem fileSystem = vertx.fileSystem();
        long deploymentId = deployRequest.getDeployment().getDeploymentId();
        TerraformModule deployModule = new ServiceModule("service_deploy",
            ModuleType.SERVICE_DEPLOY);
        Path deployDir = deploymentPath.getModuleFolder(deployModule);
        List<Completable> completables = new ArrayList<>();
        for (ServiceDeployment serviceDeployment : serviceDeployments) {
            ServiceDeployFileService serviceDeployFileService = new ServiceDeployFileService(fileSystem,
                deployDir, serviceDeployment, deploymentId, config);
            completables.add(serviceDeployFileService.setUpDirectory());
        }
        return Completable.merge(completables)
            .toSingle(() -> deployModule);
    }
}
