package at.uibk.dps.rm.handler.deploymentexecution;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDTO;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.deployment.docker.LambdaJavaBuildService;
import at.uibk.dps.rm.service.deployment.docker.LambdaLayerService;
import at.uibk.dps.rm.service.deployment.docker.OpenFaasImageService;
import at.uibk.dps.rm.service.deployment.executor.MainTerraformExecutor;
import at.uibk.dps.rm.service.deployment.executor.TerraformExecutor;
import at.uibk.dps.rm.service.deployment.terraform.TerraformFileService;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.misc.ConsoleOutputUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.List;

/**
 * Implements methods to perform deployment and termination of resources.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class DeploymentExecutionChecker {

    private final ServiceProxyProvider serviceProxyProvider;


    /**
     * Deploy all resources from the deployResources object. The
     * docker credentials must contain valid data for all deployments that involve OpenFaaS. The
     * list of VPCs must be non-empty for EC2 deployments.
     *
     * @param deployResources the data of the deployment
     * @return a Completable
     */
    public Single<ProcessOutput> applyResourceDeployment(DeployResourcesDTO deployResources) {
        long deploymentId = deployResources.getDeployment().getDeploymentId();
        Vertx vertx = Vertx.currentContext().owner();
        return new ConfigUtility(vertx).getConfigDTO()
            .flatMap(config -> {
                DeploymentPath deploymentPath = new DeploymentPath(deploymentId, config);
                return serviceProxyProvider.getDeploymentExecutionService()
                    .packageFunctionsCode(deployResources)
                    .flatMapCompletable(functionsToDeploy -> buildAndPushOpenFaasImages(vertx,
                            deployResources.getDeploymentCredentials().getDockerCredentials(), functionsToDeploy,
                            deploymentPath)
                        .flatMapCompletable(dockerOutput -> persistLogs(dockerOutput, deployResources.getDeployment()))
                        .andThen(buildJavaLambdaFaaS(deployResources.getFunctionDeployments(), deploymentPath,
                            config.getDindDirectory()))
                        .flatMapCompletable(dockerOutput -> persistLogs(dockerOutput, deployResources.getDeployment()))
                        .andThen(buildLambdaLayers(deployResources.getFunctionDeployments(), deploymentPath,
                            config.getDindDirectory()))
                        .flatMapCompletable(dockerOutput -> persistLogs(dockerOutput, deployResources.getDeployment())))
                    .andThen(serviceProxyProvider.getDeploymentExecutionService().setUpTFModules(deployResources))
                    .flatMap(deploymentCredentials -> {
                        TerraformExecutor terraformExecutor = new MainTerraformExecutor(deploymentCredentials);
                        return initialiseAllContainerModules(deployResources, deploymentPath)
                            .andThen(Single.just(terraformExecutor));
                    })
                    .flatMap(terraformExecutor -> terraformExecutor.init(deploymentPath.getRootFolder())
                        .flatMapCompletable(initOutput -> persistLogs(initOutput, deployResources.getDeployment()))
                        .andThen(Single.just(terraformExecutor)))
                    .flatMap(terraformExecutor -> terraformExecutor.apply(deploymentPath.getRootFolder())
                        .flatMapCompletable(applyOutput -> persistLogs(applyOutput, deployResources.getDeployment()))
                        .andThen(Single.just(terraformExecutor)))
                    .flatMap(terraformExecutor -> terraformExecutor.getOutput(deploymentPath.getRootFolder()))
                    .flatMap(tfOutput -> persistLogs(tfOutput, deployResources.getDeployment())
                        .toSingle(() -> tfOutput));
            });
    }

    private Completable initialiseAllContainerModules(DeployResourcesDTO request, DeploymentPath deploymentPath) {
        return Observable.fromIterable(request.getServiceDeployments())
            .map(serviceDeployment -> {
                TerraformExecutor terraformExecutor = new TerraformExecutor();
                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                    String.valueOf(serviceDeployment.getResourceDeploymentId()));
                return terraformExecutor.init(containerPath)
                    .flatMapCompletable(tfOutput -> persistLogs(tfOutput, request.getDeployment()));
            }).toList()
            .flatMapCompletable(Completable::merge);
    }

  /**
   * Build docker images and push them to a docker registry.
   *
   * @param vertx the vertx instance of current context
   * @param dockerCredentials the docker credentials
   * @param functionsToDeploy the functions to deploy
   * @param deploymentPath the path of the current deployment
   * @return a Single that emits the process output of the docker process
   */
    private Single<ProcessOutput> buildAndPushOpenFaasImages(Vertx vertx, DockerCredentials dockerCredentials,
            FunctionsToDeploy functionsToDeploy, DeploymentPath deploymentPath) {
        OpenFaasImageService openFaasImageService = new OpenFaasImageService(vertx, dockerCredentials,
            functionsToDeploy, deploymentPath.getFunctionsFolder());
        return openFaasImageService.buildOpenFaasImages();
    }

  /**
   * Build all java lambda functions that are part of the function deployments.
   *
   * @param functionDeployments the function deployments
   * @param deploymentPath the path of the current deployment
   * @return a Single that emits the process output of the docker process
   */
    private Single<ProcessOutput> buildJavaLambdaFaaS(List<FunctionDeployment> functionDeployments,
            DeploymentPath deploymentPath, String dindDirectory) {
        LambdaJavaBuildService lambdaService = new LambdaJavaBuildService(functionDeployments, deploymentPath);
        return lambdaService.buildAndZipJavaFunctions(dindDirectory);
    }

  /**
   * Build all lambda layers that are required for the function deployments.
   *
   * @param functionDeployments the function deployments
   * @param deploymentPath the path of the current deployment
   * @return a Single that emits the process output of the docker process
   */
    private Single<ProcessOutput> buildLambdaLayers(List<FunctionDeployment> functionDeployments,
            DeploymentPath deploymentPath, String dindDirectory) {
        LambdaLayerService layerService = new LambdaLayerService(functionDeployments, deploymentPath);
        return  layerService.buildLambdaLayers(dindDirectory);
    }

  /**
   * Persist a process output as a deployment Log.
   *
   * @param processOutput the process output
   * @param deployment the deployment
   * @return a Completable
   */
    private Completable persistLogs(ProcessOutput processOutput, Deployment deployment) {
        if (processOutput.getProcess() == null) {
            return Completable.complete();
        }

        Log log = new Log();
        String output = ConsoleOutputUtility.escapeConsoleOutput(processOutput.getOutput());
        log.setLogValue(output);
        return serviceProxyProvider.getLogService().save(JsonObject.mapFrom(log))
            .map(logResult -> {
                Log logStored = logResult.mapTo(Log.class);
                DeploymentLog deploymentLog = new DeploymentLog();
                deploymentLog.setDeployment(deployment);
                deploymentLog.setLog(logStored);
                return serviceProxyProvider.getDeploymentLogService().save(JsonObject.mapFrom(deploymentLog));
            })
            .flatMapCompletable(res -> {
                if (processOutput.getProcess().exitValue() != 0) {
                    return Completable.error(new DeploymentTerminationFailedException());
                }
                return Completable.complete();
            });
    }

  /**
   * Terminate resources of a deployment.
   *
   * @param terminateResources the object containing all data that is necessary for the termination
   * @return a Completable
   */
    public Completable terminateResources(TerminateResourcesDTO terminateResources) {
        long deploymentId = terminateResources.getDeployment().getDeploymentId();
        Vertx vertx = Vertx.currentContext().owner();
        return new ConfigUtility(vertx).getConfigDTO()
            .flatMapCompletable(config -> {
                DeploymentPath deploymentPath = new DeploymentPath(deploymentId, config);
                return terminateAllContainerResources(terminateResources, deploymentPath)
                    .andThen(serviceProxyProvider.getDeploymentExecutionService()
                        .getNecessaryCredentials(terminateResources))
                    .map(MainTerraformExecutor::new)
                    .flatMap(terraformExecutor -> terraformExecutor.destroy(deploymentPath.getRootFolder()))
                    .flatMapCompletable(terminateOutput -> persistLogs(terminateOutput, terminateResources.getDeployment()));
            });
    }

    /**
     * Terminate all container resources from a deployment.
     *
     * @param request the request containing all data that is necessary for the termination
     * @param deploymentPath the deployment path of the deployment
     * @return a Completable
     */
    public Completable terminateAllContainerResources(TerminateResourcesDTO request, DeploymentPath deploymentPath) {
        return Observable.fromIterable(request.getServiceDeployments())
            .map(serviceDeployment -> {
                TerraformExecutor terraformExecutor = new TerraformExecutor();
                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                    String.valueOf(serviceDeployment.getResourceDeploymentId()));
                return terraformExecutor.destroy(containerPath)
                    .flatMapCompletable(tfOutput -> persistLogs(tfOutput, request.getDeployment()));
            }).toList()
            .flatMapCompletable(Completable::merge);
    }
    /**
     * Start a container from a deployment.
     *
     * @param serviceDeployment the service deployment
     * @return a Completable
     */
    public Single<JsonObject> startContainer(ServiceDeployment serviceDeployment) {
        Vertx vertx = Vertx.currentContext().owner();
        Deployment deployment = new Deployment();
        deployment.setDeploymentId(serviceDeployment.getDeployment().getDeploymentId());
        return new ConfigUtility(vertx).getConfigDTO()
            .flatMap(config -> {
                TerraformExecutor terraformExecutor = new TerraformExecutor();
                DeploymentPath deploymentPath = new DeploymentPath(deployment.getDeploymentId(), config);
                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                    String.valueOf(serviceDeployment.getResourceDeploymentId()));
                return terraformExecutor.apply(containerPath)
                    .flatMapCompletable(applyOutput -> persistLogs(applyOutput, deployment))
                    .andThen(terraformExecutor.getOutput(containerPath))
                    .flatMap(tfOutput -> persistLogs(tfOutput, deployment)
                        .toSingle(() -> {
                            JsonObject jsonOutput = new JsonObject(tfOutput.getOutput());
                            return jsonOutput.getJsonObject("deployment_data").getJsonObject("value");
                        })
                    );
            });
    }

    /**
     * Stop a container from a deployment.
     *
     * @param serviceDeployment the service deployment
     * @return a Completable
     */
    public Completable stopContainer(ServiceDeployment serviceDeployment) {
        Vertx vertx = Vertx.currentContext().owner();
        Deployment deployment = new Deployment();
        deployment.setDeploymentId(serviceDeployment.getDeployment().getDeploymentId());
        return new ConfigUtility(vertx).getConfigDTO()
            .flatMapCompletable(config -> {
                TerraformExecutor terraformExecutor = new TerraformExecutor();
                DeploymentPath deploymentPath = new DeploymentPath(deployment.getDeploymentId(), config);
                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                    String.valueOf(serviceDeployment.getResourceDeploymentId()));
                return terraformExecutor.destroy(containerPath)
                    .flatMapCompletable(destroyOutput -> persistLogs(destroyOutput, deployment));
            });
    }

    /**
     * Delete all folders and files that were created for the deployment. This
     * can be used after the termination of all resources is done.
     *
     * @param deploymentId the id of the deployment
     * @return a Completable
     */
    public Completable deleteTFDirs(long deploymentId) {
        Vertx vertx = Vertx.currentContext().owner();
        return new ConfigUtility(vertx).getConfigDTO().flatMapCompletable(config -> {
            DeploymentPath deploymentPath = new DeploymentPath(deploymentId, config);
            return TerraformFileService.deleteAllDirs(vertx.fileSystem(), deploymentPath.getRootFolder());
        });
    }

    /**
     * Check whether a terraform lock file exists at the tfPath or not. The existence
     * of this file indicates, that resources may be deployed.
     *
     * @param tfPath the root path to a terraform module
     * @return a Single that emits true if the lock file exists, else false
     */
    public Single<Boolean> tfLockFileExists(String tfPath) {
        Path lockFilePath = Path.of(tfPath, ".terraform.lock.hcl");
        Vertx vertx = Vertx.currentContext().owner();
        return vertx.fileSystem().exists(lockFilePath.toString());
    }
}
