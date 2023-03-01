package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.model.ResourceReservationStatus;
import at.uibk.dps.rm.repository.reservation.ResourceReservationStatusRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class ResourceReservationStatusServiceImpl  extends ServiceProxy<ResourceReservationStatus>
    implements ResourceReservationStatusService {

    private final ResourceReservationStatusRepository repository;

    public ResourceReservationStatusServiceImpl(ResourceReservationStatusRepository repository) {
        super(repository, ResourceReservationStatus.class);
        this.repository = repository;
    }

    @Override
    public Future<JsonObject> findOneByStatusValue(String statusValue) {
        return Future
            .fromCompletionStage(repository.findOneByStatusValue(statusValue))
            .map(JsonObject::mapFrom);
    }
}
