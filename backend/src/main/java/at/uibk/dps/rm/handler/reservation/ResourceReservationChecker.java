package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResourceReservationChecker extends EntityChecker {

    private final ResourceReservationService resourceReservationService;

    public ResourceReservationChecker(ResourceReservationService resourceReservationService) {
        super(resourceReservationService);
        this.resourceReservationService = resourceReservationService;
    }

    public Single<JsonArray> checkFindAllByReservationId(long id) {
        Single<JsonArray> findAllByResourceId = resourceReservationService.findAllByReservationId(id);
        return ErrorHandler.handleFindAll(findAllByResourceId);
    }

    public Completable submitUpdateStatus(long reservationId, ReservationStatusValue statusValue) {
        return resourceReservationService.updateSetStatusByReservationId(reservationId, statusValue);
    }



    public Completable storeOutputToFunctionResources(ProcessOutput processOutput, DeployResourcesRequest request) {
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

    private List<Completable> setTriggerUrlsByResourceTypeSet(Set<Map.Entry<String, String>> resourceTypeSet,
                                                              DeployResourcesRequest request) {
        List<Completable> completables = new ArrayList<>();
        for (Map.Entry<String, String> entry : resourceTypeSet) {
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
}
