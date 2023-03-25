package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.TerminateResourcesRequest;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.entity.model.ReservationLog;
import at.uibk.dps.rm.exception.DeploymentFailedException;
import at.uibk.dps.rm.service.deployment.docker.DockerImageService;
import at.uibk.dps.rm.service.deployment.executor.TerraformExecutor;
import at.uibk.dps.rm.service.rxjava3.database.log.LogService;
import at.uibk.dps.rm.service.rxjava3.database.log.ReservationLogService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentService;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.util.ConsoleOutputUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class DeploymentChecker {

    private final DeploymentService deploymentService;

    private final LogService logService;

    private final ReservationLogService reservationLogService;

    private final ResourceReservationService resourceReservationService;

    public DeploymentChecker(DeploymentService deploymentService, LogService logService,
                             ReservationLogService reservationLogService,
                             ResourceReservationService resourceReservationService) {
        this.deploymentService = deploymentService;
        this.logService = logService;
        this.reservationLogService = reservationLogService;
        this.resourceReservationService = resourceReservationService;
    }

    public Completable deployResources(DeployResourcesRequest request) {
        DeploymentPath deploymentPath = new DeploymentPath(request.getReservation().getReservationId());
        Vertx vertx = Vertx.currentContext().owner();

        return deploymentService.packageFunctionsCode(request)
            .flatMap(functionsToDeploy -> buildAndPushDockerImages(vertx, request, functionsToDeploy, deploymentPath))
            .flatMapCompletable(dockerOutput -> persistLogs(dockerOutput, request.getReservation()))
            .andThen(deploymentService.setUpTFModules(request))
            .flatMap(deploymentCredentials -> {
                TerraformExecutor terraformExecutor = new TerraformExecutor(vertx, deploymentCredentials);
                return Single.fromCallable(() -> terraformExecutor.setPluginCacheFolder(deploymentPath.getTFCacheFolder()))
                    .map(res -> terraformExecutor);
            })
            .flatMap(terraformExecutor -> terraformExecutor.init(deploymentPath.getRootFolder())
                .flatMapCompletable(initOutput -> persistLogs(initOutput, request.getReservation()))
                .andThen(Single.just(terraformExecutor)))
            .flatMap(terraformExecutor -> terraformExecutor.apply(deploymentPath.getRootFolder())
                .flatMapCompletable(applyOutput -> persistLogs(applyOutput, request.getReservation()))
                .andThen(Single.just(terraformExecutor)))
            .flatMap(terraformExecutor -> terraformExecutor.getOutput(deploymentPath.getRootFolder()))
            .flatMapCompletable(tfOutput -> persistLogs(tfOutput, request.getReservation())
                .andThen(storeOutputToFunctionResources(tfOutput, request)));
    }

    private Single<ProcessOutput> buildAndPushDockerImages(Vertx vertx, DeployResourcesRequest request,
                                                           FunctionsToDeploy functionsToDeploy, DeploymentPath deploymentPath) {
        DockerImageService dockerImageService = new DockerImageService(vertx, request.getDockerCredentials(),
            functionsToDeploy.getFunctionIdentifiers(), deploymentPath.getFunctionsFolder());
        return dockerImageService.buildAndPushDockerImages(functionsToDeploy.getFunctionsString().toString());
    }

    private Completable persistLogs(ProcessOutput processOutput, Reservation reservation) {
        if (processOutput.getProcess() == null) {
            return Completable.complete();
        }

        Log log = new Log();
        String output = processOutput.getProcessOutput();
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
                    return resourceReservationService
                        .updateSetStatusByReservationId(reservation.getReservationId(), ReservationStatusValue.ERROR)
                        .andThen(Completable.defer(() -> Completable.error(new DeploymentFailedException())));
                }
                return Completable.complete();
            });
    }

    private Completable storeOutputToFunctionResources(ProcessOutput processOutput, DeployResourcesRequest request) {
        DeploymentOutput deploymentOutput = DeploymentOutput.fromJson(new JsonObject(processOutput.getProcessOutput()));
        List<Completable> completables = new ArrayList<>();
        completables.addAll(setTriggerUrlsByResourceTypeSet(deploymentOutput.getEdgeUrls().getValue().entrySet(),
            request));
        completables.addAll(setTriggerUrlsByResourceTypeSet(deploymentOutput.getFunctionUrls().getValue().entrySet(),
            request));
        completables.addAll(setTriggerUrlsByResourceTypeSet(deploymentOutput.getVmUrls().getValue().entrySet(),
            request));
        return Completable.merge(completables);
    }

    private List<Completable> setTriggerUrlsByResourceTypeSet(Set<Entry<String, String>> resourceTypeSet,
                                                 DeployResourcesRequest request) {
        List<Completable> completables = new ArrayList<>();
        for (Entry<String, String> entry : resourceTypeSet) {
            String[] entryInfo = entry.getKey().split("_");
            long resourceId = Long.parseLong(entryInfo[0].substring(1));
            String functionName = entryInfo[1];
            findFunctionResourceAndUpdateTriggerUrl(request, resourceId, functionName, entry.getValue(), completables);
        }
        return completables;
    }

    private void findFunctionResourceAndUpdateTriggerUrl(DeployResourcesRequest request, long resourceId,
                                                         String functionName, String triggerUrl,
                                                         List<Completable> completables) {
        request.getFunctionResources().stream()
            .filter(functionResource -> matchesFunctionResource(resourceId, functionName, functionResource))
            .findFirst()
            .ifPresent(functionResource ->
                completables.add(resourceReservationService.updateTriggerUrl(functionResource.getFunctionResourceId(),
                    request.getReservation().getReservationId(), triggerUrl))
            );
    }

    private static boolean matchesFunctionResource(long resourceId, String functionName, FunctionResource functionResource) {
        return functionResource.getResource().getResourceId() == resourceId &&
            functionResource.getFunction().getName().equals(functionName);
    }

    public Completable terminateResources(TerminateResourcesRequest request) {
        DeploymentPath deploymentPath = new DeploymentPath(request.getReservation().getReservationId());
        Vertx vertx = Vertx.currentContext().owner();
        return deploymentService.getNecessaryCredentials(request)
            .map(deploymentCredentials -> new TerraformExecutor(vertx, deploymentCredentials))
            .flatMap(terraformExecutor -> terraformExecutor.destroy(deploymentPath.getRootFolder()))
            .flatMapCompletable(terminateOutput -> persistLogs(terminateOutput, request.getReservation()));
    }

    public Completable deleteTFDirs(long reservationId) {
        return deploymentService.deleteTFDirs(reservationId);
    }
}
