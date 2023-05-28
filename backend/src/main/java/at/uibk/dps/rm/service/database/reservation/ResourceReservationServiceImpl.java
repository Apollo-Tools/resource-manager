package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.model.FunctionReservation;
import at.uibk.dps.rm.entity.model.ResourceReservation;
import at.uibk.dps.rm.entity.model.ServiceReservation;
import at.uibk.dps.rm.repository.reservation.ResourceReservationRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

/**
 * This is the implementation of the #ResourceReservationService.
 *
 * @author matthi-g
 */
public class ResourceReservationServiceImpl extends DatabaseServiceProxy<ResourceReservation> implements ResourceReservationService {

    private final ResourceReservationRepository resourceReservationRepository;

    /**
     * Create aninstance from the resourceReservationRepositry.
     *
     * @param resourceReservationRepository the resource reservation repository
     */
    public ResourceReservationServiceImpl(ResourceReservationRepository resourceReservationRepository) {
        super(resourceReservationRepository, ResourceReservation.class);
        this.resourceReservationRepository = resourceReservationRepository;
    }

    @Override
    public Future<JsonArray> findAllByReservationId(long reservationId) {
        return Future
                .fromCompletionStage(resourceReservationRepository.findAllByReservationId(reservationId))
                .map(result -> {
                    ArrayList<JsonObject> objects = new ArrayList<>();
                    for (ResourceReservation entity: result) {
                        // TODO: fix
                        if (entity instanceof ServiceReservation) {
                            ((ServiceReservation) entity).setService(null);
                        } else if (entity instanceof FunctionReservation) {
                            ((FunctionReservation) entity).setFunction(null);
                        }
                        entity.setReservation(null);
                        entity.setResource(null);
                        objects.add(JsonObject.mapFrom(entity));
                    }
                    return new JsonArray(objects);
                });
    }

    @Override
    public Future<Void> updateTriggerUrl(long id, String triggerUrl) {
        return Future
            .fromCompletionStage(resourceReservationRepository.updateTriggerUrl(id, triggerUrl))
            .mapEmpty();
    }

    @Override
    public Future<Void> updateSetStatusByReservationId(long reservationId, ReservationStatusValue reservationStatusValue) {
        return Future
            .fromCompletionStage(resourceReservationRepository
                .updateReservationStatusByReservationId(reservationId, reservationStatusValue))
            .mapEmpty();
    }
}
