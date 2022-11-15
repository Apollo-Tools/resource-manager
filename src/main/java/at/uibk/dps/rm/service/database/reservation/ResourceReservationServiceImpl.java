package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.model.ResourceReservation;
import at.uibk.dps.rm.repository.ResourceReservationRepository;
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

    public Future<JsonArray> findAllByReservationId(long id) {
        return Future
                .fromCompletionStage(resourceReservationRepository.findAllByReservationId(id))
                .map(result -> {
                    ArrayList<JsonObject> objects = new ArrayList<>();
                    for (ResourceReservation entity: result) {
                        entity.setReservation(null);
                        entity.getResource().setResourceType(null);
                        entity.getResource().setMetricValues(null);
                        objects.add(JsonObject.mapFrom(entity));
                    }
                    return new JsonArray(objects);
                });
    }
}
