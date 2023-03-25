package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.model.ResourceReservation;
import at.uibk.dps.rm.repository.reservation.ResourceReservationRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

public class ResourceReservationServiceImpl extends ServiceProxy<ResourceReservation> implements ResourceReservationService {

    private final ResourceReservationRepository resourceReservationRepository;

    public ResourceReservationServiceImpl(ResourceReservationRepository resourceReservationRepository) {
        super(resourceReservationRepository, ResourceReservation.class);
        this.resourceReservationRepository = resourceReservationRepository;
    }

    @Override
    public Future<JsonArray> findAllByReservationId(long id) {
        return Future
                .fromCompletionStage(resourceReservationRepository.findAllByReservationId(id))
                .map(result -> {
                    ArrayList<JsonObject> objects = new ArrayList<>();
                    for (ResourceReservation entity: result) {
                        entity.setReservation(null);
                        // TODO: fix
                        entity.getFunctionResource().getResource().setResourceType(null);
                        entity.getFunctionResource().getResource().setMetricValues(null);
                        entity.getFunctionResource().getResource().setRegion(null);
                        entity.getFunctionResource().getFunction().setRuntime(null);
                        objects.add(JsonObject.mapFrom(entity));
                    }
                    return new JsonArray(objects);
                });
    }

    @Override
    public Future<Void> updateTriggerUrl(long functionResourceId, long reservationId, String triggerUrl) {
        return Future
            .fromCompletionStage(resourceReservationRepository.updateTriggerUrl(functionResourceId, reservationId,
                    triggerUrl))
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
