package at.uibk.dps.rm.handler.deploymentexecution;

import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDTO;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.DeploymentLog;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.service.deployment.docker.LambdaJavaBuildService;
import at.uibk.dps.rm.service.deployment.docker.LambdaLayerService;
import at.uibk.dps.rm.service.deployment.docker.OpenFaasImageService;
import at.uibk.dps.rm.service.deployment.executor.MainTerraformExecutor;
import at.uibk.dps.rm.service.deployment.executor.TerraformExecutor;
import at.uibk.dps.rm.service.rxjava3.database.log.LogService;
import at.uibk.dps.rm.service.rxjava3.database.log.DeploymentLogService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentExecutionService;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.misc.ConsoleOutputUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;

import java.nio.file.Path;
import java.util.List;

/**
 * Implements methods to perform deployment and termination of resources.
 *
 * @author matthi-g
 */
public class DeploymentExecutionChecker {

    private final DeploymentExecutionService deploymentService;

    private final LogService logService;

    private final DeploymentLogService deploymentLogService;

    /**
    * Create an instance from the deploymentService, logService and deploymentLogService.
    *
    * @param deploymentService the deployment service
    * @param logService the log service
    * @param deploymentLogService the deployment log service
    */
    public DeploymentExecutionChecker(DeploymentExecutionService deploymentService, LogService logService,
      DeploymentLogService deploymentLogService) {
    this.deploymentService = deploymentService;
    this.logService = logService;
    this.deploymentLogService = deploymentLogService;
    }

    /**
    * Deploy resources at multiple regions.
    *
    * @param request the request containing all data that is necessary for the deployment
    * @return a Single that emits the process output of the last step.
    */
    public Single<ProcessOutput> applyResourceDeployment(DeployResourcesDTO request) {
        long deploymentId = request.getDeployment().getDeploymentId();
        Vertx vertx = Vertx.currentContext().owner();
        return new ConfigUtility(vertx).getConfig()
            .flatMap(config -> {
                DeploymentPath deploymentPath = new DeploymentPath(deploymentId, config);
                return deploymentService.packageFunctionsCode(request)
                    .flatMapCompletable(functionsToDeploy -> buildAndPushOpenFaasImages(vertx,
                            request.getDeploymentCredentials().getDockerCredentials(), functionsToDeploy,
                            deploymentPath)
                        .flatMapCompletable(dockerOutput -> persistLogs(dockerOutput, request.getDeployment()))
                        .andThen(buildJavaLambdaFaaS(request.getFunctionDeployments(), deploymentPath))
                        .flatMapCompletable(dockerOutput -> persistLogs(dockerOutput, request.getDeployment()))
                        .andThen(buildLambdaLayers(request.getFunctionDeployments(), deploymentPath))
                        .flatMapCompletable(dockerOutput -> persistLogs(dockerOutput, request.getDeployment())))
                    .andThen(deploymentService.setUpTFModules(request))
                    .flatMap(deploymentCredentials -> {
                        TerraformExecutor terraformExecutor = new MainTerraformExecutor(vertx, deploymentCredentials);
                        return Single.fromCallable(() ->
                                terraformExecutor.setPluginCacheFolder(deploymentPath.getTFCacheFolder()))
                            .flatMapCompletable(res -> initialiseAllContainerModules(request, deploymentPath))
                            .andThen(Single.just(terraformExecutor));
                    })
                    .flatMap(terraformExecutor -> terraformExecutor.init(deploymentPath.getRootFolder())
                        .flatMapCompletable(initOutput -> persistLogs(initOutput, request.getDeployment()))
                        .andThen(Single.just(terraformExecutor)))
                    .flatMap(terraformExecutor -> terraformExecutor.apply(deploymentPath.getRootFolder())
                        .flatMapCompletable(applyOutput -> persistLogs(applyOutput, request.getDeployment()))
                        .andThen(Single.just(terraformExecutor)))
                    .flatMap(terraformExecutor -> terraformExecutor.getOutput(deploymentPath.getRootFolder()))
                    .flatMap(tfOutput -> persistLogs(tfOutput, request.getDeployment())
                        .toSingle(() -> tfOutput));
            });
    }

    private Completable initialiseAllContainerModules(DeployResourcesDTO request, DeploymentPath deploymentPath) {
        return Observable.fromIterable(request.getServiceDeployments())
            .map(serviceDeployment -> {
                TerraformExecutor terraformExecutor = new TerraformExecutor(Vertx.currentContext().owner());
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
    private Single<ProcessOutput> buildAndPushOpenFaasImages(Vertx vertx, List<DockerCredentials> dockerCredentials,
            FunctionsToDeploy functionsToDeploy, DeploymentPath deploymentPath) {
        OpenFaasImageService openFaasImageService = new OpenFaasImageService(vertx, dockerCredentials,
            functionsToDeploy.getDockerFunctionIdentifiers(), deploymentPath.getFunctionsFolder());
        return openFaasImageService.buildOpenFaasImages(functionsToDeploy.getDockerFunctionsString());
    }

  /**
   * Build all java lambda functions that are part of the function deployments.
   *
   * @param functionDeployments the function deployments
   * @param deploymentPath the path of the current deployment
   * @return a Single that emits the process output of the docker process
   */
    private Single<ProcessOutput> buildJavaLambdaFaaS(List<FunctionDeployment> functionDeployments,
            DeploymentPath deploymentPath) {
        LambdaJavaBuildService lambdaService = new LambdaJavaBuildService(functionDeployments, deploymentPath);
        return new ConfigUtility(Vertx.currentContext().owner()).getConfig()
            .flatMap(config ->lambdaService.buildAndZipJavaFunctions(config.getString("dind_directory")));
    }

  /**
   * Build all lambda layers that are required for the function deployments.
   *
   * @param functionDeployments the function deployments
   * @param deploymentPath the path of the current deployment
   * @return a Single that emits the process output of the docker process
   */
    private Single<ProcessOutput> buildLambdaLayers(List<FunctionDeployment> functionDeployments,
            DeploymentPath deploymentPath) {
        LambdaLayerService layerService = new LambdaLayerService(functionDeployments, deploymentPath);
        return  new ConfigUtility(Vertx.currentContext().owner()).getConfig()
            .flatMap(config -> layerService.buildLambdaLayers(config.getString("dind_directory")));
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
        return logService.save(JsonObject.mapFrom(log))
            .map(logResult -> {
                Log logStored = logResult.mapTo(Log.class);
                DeploymentLog deploymentLog = new DeploymentLog();
                deploymentLog.setDeployment(deployment);
                deploymentLog.setLog(logStored);
                return deploymentLogService.save(JsonObject.mapFrom(deploymentLog));
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
        return new ConfigUtility(vertx).getConfig()
            .flatMapCompletable(config -> {
                DeploymentPath deploymentPath = new DeploymentPath(deploymentId, config);
                return terminateAllContainerResources(terminateResources, deploymentPath)
                    .andThen(deploymentService.getNecessaryCredentials(terminateResources))
                    .map(deploymentCredentials -> new MainTerraformExecutor(vertx, deploymentCredentials))
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
                TerraformExecutor terraformExecutor = new TerraformExecutor(Vertx.currentContext().owner());
                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                    String.valueOf(serviceDeployment.getResourceDeploymentId()));
                return terraformExecutor.destroy(containerPath)
                    .flatMapCompletable(tfOutput -> persistLogs(tfOutput, request.getDeployment()));
            }).toList()
            .flatMapCompletable(Completable::merge);
    }

    /**
    * Delete all folders and files that were created for the deployment. Usually this
    * gets called when the termination of all resources is done.
    *
    * @param deploymentId the id of the deployment
    * @return a Completable
    */
    public Completable deleteTFDirs(long deploymentId) {
        return deploymentService.deleteTFDirs(deploymentId);
    }

    /**
     * Start a container from a deployment.
     *
     * @param deploymentId the id of the deployment
     * @param resourceDeploymentId the id of the resource deployment
     * @return a Completable
     */
    public Completable startContainer(long deploymentId, long resourceDeploymentId) {
        Vertx vertx = Vertx.currentContext().owner();
        Deployment deployment = new Deployment();
        deployment.setDeploymentId(deploymentId);
        return new ConfigUtility(vertx).getConfig()
            .flatMapCompletable(config -> {
                TerraformExecutor terraformExecutor = new TerraformExecutor(vertx);
                DeploymentPath deploymentPath = new DeploymentPath(deploymentId, config);
                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                    String.valueOf(resourceDeploymentId));
                return terraformExecutor.apply(containerPath)
                    .flatMapCompletable(applyOutput -> persistLogs(applyOutput, deployment));
            });
    }

    /**
     * Stop a container from a deployment.
     *
     * @param deploymentId the id of the deployment
     * @param resourceDeploymentId the id of the resource deployment
     * @return a Completable
     */
    public Completable stopContainer(long deploymentId, long resourceDeploymentId) {
        Vertx vertx = Vertx.currentContext().owner();
        Deployment deployment = new Deployment();
        deployment.setDeploymentId(deploymentId);
        return new ConfigUtility(vertx).getConfig()
            .flatMapCompletable(config -> {
                TerraformExecutor terraformExecutor = new TerraformExecutor(vertx);
                DeploymentPath deploymentPath = new DeploymentPath(deploymentId, config);
                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                    String.valueOf(resourceDeploymentId));
                return terraformExecutor.destroy(containerPath)
                    .flatMapCompletable(destroyOutput -> persistLogs(destroyOutput, deployment));
            });
    }
}
