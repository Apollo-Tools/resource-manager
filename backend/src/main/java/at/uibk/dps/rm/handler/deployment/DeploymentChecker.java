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
import at.uibk.dps.rm.service.deployment.executor.TerraformExecutor;
import at.uibk.dps.rm.service.rxjava3.database.log.LogService;
import at.uibk.dps.rm.service.rxjava3.database.log.ReservationLogService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentService;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.util.ConfigUtility;
import at.uibk.dps.rm.util.ConsoleOutputUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;

public class DeploymentChecker {

    private final DeploymentService deploymentService;

    private final LogService logService;

    private final ReservationLogService reservationLogService;

    public DeploymentChecker(DeploymentService deploymentService, LogService logService,
                             ReservationLogService reservationLogService) {
        this.deploymentService = deploymentService;
        this.logService = logService;
        this.reservationLogService = reservationLogService;
    }

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
                        TerraformExecutor terraformExecutor = new TerraformExecutor(vertx, deploymentCredentials);
                        return Single.fromCallable(() ->
                                terraformExecutor.setPluginCacheFolder(deploymentPath.getTFCacheFolder()))
                            .map(res -> terraformExecutor);
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

    private Single<ProcessOutput> buildAndPushDockerImages(Vertx vertx, DeployResourcesRequest request,
                                                           FunctionsToDeploy functionsToDeploy, DeploymentPath deploymentPath) {
        DockerImageService dockerImageService = new DockerImageService(vertx, request.getDockerCredentials(),
            functionsToDeploy.getFunctionIdentifiers(), deploymentPath.getFunctionsFolder());
        return dockerImageService.buildAndPushDockerImages(functionsToDeploy.getFunctionsString());
    }

    private Completable persistLogs(ProcessOutput processOutput, Reservation reservation) {
        if (processOutput.getProcess() == null) {
            return Completable.complete();
        }

        Log log = new Log();
        String output = processOutput.getOutput();
        output = ConsoleOutputUtility.escapeConsoleOutput(output);
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

    public Completable terminateResources(TerminateResourcesRequest request) {
        long reservationId = request.getReservation().getReservationId();
        Vertx vertx = Vertx.currentContext().owner();
        return new ConfigUtility(vertx).getConfig()
            .flatMapCompletable(config -> {
                DeploymentPath deploymentPath = new DeploymentPath(reservationId, config);
                return deploymentService.getNecessaryCredentials(request)
                    .map(deploymentCredentials -> new TerraformExecutor(vertx, deploymentCredentials))
                    .flatMap(terraformExecutor -> terraformExecutor.destroy(deploymentPath.getRootFolder()))
                    .flatMapCompletable(terminateOutput -> persistLogs(terminateOutput, request.getReservation()));
            });
    }

    public Completable deleteTFDirs(long reservationId) {
        return deploymentService.deleteTFDirs(reservationId);
    }
}
