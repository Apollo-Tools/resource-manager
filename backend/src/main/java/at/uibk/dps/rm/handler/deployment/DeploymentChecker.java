package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.TerminateResourcesRequest;
import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.entity.model.ReservationLog;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.service.deployment.docker.DockerImageService;
import at.uibk.dps.rm.service.deployment.executor.MainTerraformExecutor;
import at.uibk.dps.rm.service.deployment.executor.TerraformExecutor;
import at.uibk.dps.rm.service.rxjava3.database.log.LogService;
import at.uibk.dps.rm.service.rxjava3.database.log.ReservationLogService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentService;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.misc.ConsoleOutputUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;

import java.nio.file.Path;

/**
 * Implements methods to perform deployment and termination of resources.
 *
 * @author matthi-g
 */
public class DeploymentChecker {

    private final DeploymentService deploymentService;

    private final LogService logService;

    private final ReservationLogService reservationLogService;

    /**
    * Create an instance from the deploymentService, logService and reservationLogService.
    *
    * @param deploymentService the deployment service
    * @param logService the log service
    * @param reservationLogService the reservation service
    */
    public DeploymentChecker(DeploymentService deploymentService, LogService logService,
      ReservationLogService reservationLogService) {
    this.deploymentService = deploymentService;
    this.logService = logService;
    this.reservationLogService = reservationLogService;
    }

    /**
    * Deploy resources at multiple regions.
    *
    * @param request the request containing all data that is necessary for the deployment
    * @return a Single that emits the process output of the last step.
    */
    public Single<ProcessOutput> deployResources(DeployResourcesRequest request) {
        long reservationId = request.getReservation().getReservationId();
        Vertx vertx = Vertx.currentContext().owner();
        return new ConfigUtility(vertx).getConfig()
            .flatMap(config -> {
                DeploymentPath deploymentPath = new DeploymentPath(reservationId, config);
                return deploymentService.packageFunctionsCode(request)
                    .flatMap(functionsToDeploy -> buildAndPushDockerImages(vertx, request, functionsToDeploy, deploymentPath))
                    .flatMapCompletable(dockerOutput -> persistLogs(dockerOutput, request.getReservation()))
                    .andThen(deploymentService.setUpTFModules(request))
                    .flatMap(deploymentCredentials -> {
                        TerraformExecutor terraformExecutor = new MainTerraformExecutor(vertx, deploymentCredentials);
                        return Single.fromCallable(() ->
                                terraformExecutor.setPluginCacheFolder(deploymentPath.getTFCacheFolder()))
                            .flatMapCompletable(res -> initialiseAllContainerModules(request, deploymentPath))
                            .andThen(Single.just(terraformExecutor));
                    })
                    .flatMap(terraformExecutor -> terraformExecutor.init(deploymentPath.getRootFolder())
                        .flatMapCompletable(initOutput -> persistLogs(initOutput, request.getReservation()))
                        .andThen(Single.just(terraformExecutor)))
                    .flatMap(terraformExecutor -> terraformExecutor.apply(deploymentPath.getRootFolder())
                        .flatMapCompletable(applyOutput -> persistLogs(applyOutput, request.getReservation()))
                        .andThen(Single.just(terraformExecutor)))
                    .flatMap(terraformExecutor -> terraformExecutor.getOutput(deploymentPath.getRootFolder()))
                    .flatMap(tfOutput -> persistLogs(tfOutput, request.getReservation())
                        .toSingle(() -> tfOutput));
            });
    }

    private Completable initialiseAllContainerModules(DeployResourcesRequest request, DeploymentPath deploymentPath) {
        return Observable.fromIterable(request.getServiceReservations())
            .map(serviceReservation -> {
                TerraformExecutor terraformExecutor = new TerraformExecutor(Vertx.currentContext().owner());
                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                    String.valueOf(serviceReservation.getResourceReservationId()));
                return terraformExecutor.init(containerPath)
                    .flatMapCompletable(tfOutput -> persistLogs(tfOutput, request.getReservation()));
            }).toList()
            .flatMapCompletable(Completable::merge);
    }

  /**
   * Build docker images and push them to a docker registry.
   *
   * @param vertx the vertx instance of current context
   * @param request the deploy resources request containing all deployment data
   * @param functionsToDeploy the functions to deploy
   * @param deploymentPath the path of the current deployment
   * @return a Single that emits the process output of the docker process
   */
    private Single<ProcessOutput> buildAndPushDockerImages(Vertx vertx, DeployResourcesRequest request,
        FunctionsToDeploy functionsToDeploy, DeploymentPath deploymentPath) {
        DockerImageService dockerImageService = new DockerImageService(vertx, request.getDockerCredentials(),
            functionsToDeploy.getDockerFunctionIdentifiers(), deploymentPath.getFunctionsFolder());
        return dockerImageService.buildOpenFaasImages(functionsToDeploy.getDockerFunctionsString());
    }

  /**
   * Persist a process output as a reservation Log.
   *
   * @param processOutput the process output
   * @param reservation the Reservation
   * @return a Completable
   */
    private Completable persistLogs(ProcessOutput processOutput, Reservation reservation) {
        if (processOutput.getProcess() == null) {
            return Completable.complete();
        }

        Log log = new Log();
        String output = ConsoleOutputUtility.escapeConsoleOutput(processOutput.getOutput());
        log.setLogValue(output);
        return logService.save(JsonObject.mapFrom(log))
            .map(logResult -> {
                Log logStored = logResult.mapTo(Log.class);
                ReservationLog reservationLog = new ReservationLog();
                reservationLog.setReservation(reservation);
                reservationLog.setLog(logStored);
                return reservationLogService.save(JsonObject.mapFrom(reservationLog));
            })
            .flatMapCompletable(res -> {
                if (processOutput.getProcess().exitValue() != 0) {
                    return Completable.error(new DeploymentTerminationFailedException());
                }
                return Completable.complete();
            });
    }

  /**
   * Terminate resources of a reservation.
   *
   * @param request the request containing all data that is necessary for the termination
   * @return a Completable
   */
    public Completable terminateResources(TerminateResourcesRequest request) {
        long reservationId = request.getReservation().getReservationId();
        Vertx vertx = Vertx.currentContext().owner();
        return new ConfigUtility(vertx).getConfig()
            .flatMapCompletable(config -> {
                DeploymentPath deploymentPath = new DeploymentPath(reservationId, config);
                return terminateAllContainerResources(request, deploymentPath)
                    .andThen(deploymentService.getNecessaryCredentials(request))
                    .map(deploymentCredentials -> new MainTerraformExecutor(vertx, deploymentCredentials))
                    .flatMap(terraformExecutor -> terraformExecutor.destroy(deploymentPath.getRootFolder()))
                    .flatMapCompletable(terminateOutput -> persistLogs(terminateOutput, request.getReservation()));
            });
    }

    /**
     * Terminate all container resources from a reservation.
     *
     * @param request the request containing all data that is necessary for the termination
     * @param deploymentPath the deployment path of the reservation
     * @return a Completable
     */
    public Completable terminateAllContainerResources(TerminateResourcesRequest request, DeploymentPath deploymentPath) {
        return Observable.fromIterable(request.getServiceReservations())
            .map(serviceReservation -> {
                TerraformExecutor terraformExecutor = new TerraformExecutor(Vertx.currentContext().owner());
                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                    String.valueOf(serviceReservation.getResourceReservationId()));
                return terraformExecutor.destroy(containerPath)
                    .flatMapCompletable(tfOutput -> persistLogs(tfOutput, request.getReservation()));
            }).toList()
            .flatMapCompletable(Completable::merge);
    }

    /**
    * Delete all folders and files that were created for the deployment. Usually this
    * gets called when the termination of all resources is done.
    *
    * @param reservationId the id of the reservation
    * @return a Completable
    */
    public Completable deleteTFDirs(long reservationId) {
        return deploymentService.deleteTFDirs(reservationId);
    }

    /**
     * Deploy a container from a reservation.
     *
     * @param reservationId the id of the reservation
     * @param resourceReservationId the id of the resource reservation
     * @return a Completable
     */
    public Completable deployContainer(long reservationId, long resourceReservationId) {
        Vertx vertx = Vertx.currentContext().owner();
        Reservation reservation = new Reservation();
        reservation.setReservationId(reservationId);
        return new ConfigUtility(vertx).getConfig()
            .flatMapCompletable(config -> {
                TerraformExecutor terraformExecutor = new TerraformExecutor(vertx);
                DeploymentPath deploymentPath = new DeploymentPath(reservationId, config);
                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                    String.valueOf(resourceReservationId));
                return terraformExecutor.apply(containerPath)
                    .flatMapCompletable(applyOutput -> persistLogs(applyOutput, reservation));
            });
    }

    /**
     * Terminate a container from a reservation.
     *
     * @param reservationId the id of the reservation
     * @param resourceReservationId the id of the resource reservation
     * @return a Completable
     */
    public Completable terminateContainer(long reservationId, long resourceReservationId) {
        Vertx vertx = Vertx.currentContext().owner();
        Reservation reservation = new Reservation();
        reservation.setReservationId(reservationId);
        return new ConfigUtility(vertx).getConfig()
            .flatMapCompletable(config -> {
                TerraformExecutor terraformExecutor = new TerraformExecutor(vertx);
                DeploymentPath deploymentPath = new DeploymentPath(reservationId, config);
                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                    String.valueOf(resourceReservationId));
                return terraformExecutor.destroy(containerPath)
                    .flatMapCompletable(destroyOutput -> persistLogs(destroyOutput, reservation));
            });
    }
}
