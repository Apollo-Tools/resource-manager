package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.model.FunctionReservation;
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

/**
 * Implements methods to perform CRUD operations on the resource_reservation entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class ResourceReservationChecker extends EntityChecker {

    private final ResourceReservationService resourceReservationService;

    /**
     * Create an instance from a resourceReservationService.
     *
     * @param resourceReservationService the resource reservation service
     */
    public ResourceReservationChecker(ResourceReservationService resourceReservationService) {
        super(resourceReservationService);
        this.resourceReservationService = resourceReservationService;
    }

    //TODO add accountId
    /**
     * Find all resource reservations by reservation.
     *
     * @return a Single that emits all found resource reservations as JsonArray
     */
    public Single<JsonArray> checkFindAllByReservationId(long id) {
        final Single<JsonArray> findAllByResourceId = resourceReservationService.findAllByReservationId(id);
        return ErrorHandler.handleFindAll(findAllByResourceId);
    }

    /**
     * Submit the update of the status of a resource reservation.
     *
     * @param reservationId the id of the reservation
     * @param statusValue the new status
     * @return a Completable
     */
    public Completable submitUpdateStatus(long reservationId, ReservationStatusValue statusValue) {
        return resourceReservationService.updateSetStatusByReservationId(reservationId, statusValue);
    }

    /**
     * Store the trigger urls of a reservation to the resource reservations.
     *
     * @param processOutput the output of the terraform process
     * @param request all data needed for the deployment process
     * @return a Completable
     */
    public Completable storeOutputToFunctionResources(ProcessOutput processOutput, DeployResourcesRequest request) {
        DeploymentOutput deploymentOutput = DeploymentOutput.fromJson(new JsonObject(processOutput.getOutput()));
        List<Completable> completables = new ArrayList<>();
        completables.addAll(setTriggerUrlsByResourceTypeSet(deploymentOutput.getEdgeUrls().getValue().entrySet(),
            request));
        completables.addAll(setTriggerUrlsByResourceTypeSet(deploymentOutput.getFunctionUrls().getValue().entrySet(),
            request));
        completables.addAll(setTriggerUrlsByResourceTypeSet(deploymentOutput.getVmUrls().getValue().entrySet(),
            request));
        return Completable.merge(completables);
    }

    /**
     * Store all trigger urls of a reservation by resource type.
     *
     * @param resourceTypeSet all function resources of a certain resource type
     * @param request all data needed for the deployment process
     * @return a list of Completables
     */
    private List<Completable> setTriggerUrlsByResourceTypeSet(Set<Map.Entry<String, String>> resourceTypeSet,
                                                              DeployResourcesRequest request) {
        List<Completable> completables = new ArrayList<>();
        for (Map.Entry<String, String> entry : resourceTypeSet) {
            String[] entryInfo = entry.getKey().split("_");
            long resourceId = Long.parseLong(entryInfo[0].substring(1));
            String functionName = entryInfo[1], runtimeName = entryInfo[2];
            findFunctionResourceAndUpdateTriggerUrl(request, resourceId, functionName, runtimeName, entry.getValue(),
                completables);
        }
        return completables;
    }

    /**
     * Find the persisted function resource and update its trigger url.
     *
     * @param request all data needed for the deployment process
     * @param resourceId the id of the resource
     * @param functionName the name of the function
     * @param runtimeName the name of the runtime
     * @param triggerUrl the trigger url
     * @param completables the list where to store the new completables
     */
    private void findFunctionResourceAndUpdateTriggerUrl(DeployResourcesRequest request, long resourceId,
        String functionName, String runtimeName, String triggerUrl, List<Completable> completables) {
        request.getFunctionReservations().stream()
            .filter(functionReservation -> matchesFunctionResource(resourceId, functionName, runtimeName,
                functionReservation))
            .findFirst()
            .ifPresent(functionReservation -> completables
                .add(resourceReservationService.updateTriggerUrl(functionReservation.getResourceReservationId(),
                    triggerUrl))
            );
    }

    /**
     * Get the crucial resource reservation status based on all resource reservations of a single
     * reservation.
     *
     * @param resourceReservations the resource reservations
     * @return the crucial reservation status
     */
    public ReservationStatusValue checkCrucialResourceReservationStatus(JsonArray resourceReservations) {
        if (matchAnyResourceReservationsStatus(resourceReservations,
            ReservationStatusValue.ERROR)) {
            return ReservationStatusValue.ERROR;
        }
        if (matchAnyResourceReservationsStatus(resourceReservations, ReservationStatusValue.NEW)) {
            return ReservationStatusValue.NEW;
        }

        if (matchAnyResourceReservationsStatus(resourceReservations,
            ReservationStatusValue.TERMINATING)) {
            return ReservationStatusValue.TERMINATING;
        }

        if (matchAnyResourceReservationsStatus(resourceReservations,
            ReservationStatusValue.DEPLOYED)) {
            return ReservationStatusValue.DEPLOYED;
        }
        return ReservationStatusValue.TERMINATED;
    }

    /**
     * Check if at least one status of resource reservation matches the given status value.
     *
     * @param resourceReservations the resource reservations
     * @param statusValue the status value
     * @return true if at least one match was found, else false
     */
    private boolean matchAnyResourceReservationsStatus(JsonArray resourceReservations,
        ReservationStatusValue statusValue) {
        return resourceReservations.stream()
            .map(rr -> (JsonObject) rr)
            .anyMatch(rr -> rr.getJsonObject("status")
                .getString("status_value").equals(statusValue.name())
            );
    }

    /**
     * Check if the given parameters match the values of the given function resource
     *
     * @param resourceId the id of a reservation
     * @param functionName the name of the function
     * @param runtimeName the name of the runtime
     * @param functionReservation the function reservation
     * @return true if they match, else fales
     */
    private static boolean matchesFunctionResource(long resourceId, String functionName, String runtimeName,
        FunctionReservation functionReservation) {
        return functionReservation.getResource().getResourceId() == resourceId &&
            functionReservation.getFunction().getName().equals(functionName) &&
            functionReservation.getFunction().getRuntime().getName().replace(".", "").equals(runtimeName);
    }
}
