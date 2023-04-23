package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.model.ResourceReservationStatus;
import at.uibk.dps.rm.repository.reservation.ResourceReservationStatusRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * This is the implementation of the #ResourceReservationStatusService.
 *
 * @author matthi-g
 */
public class ResourceReservationStatusServiceImpl  extends DatabaseServiceProxy<ResourceReservationStatus>
    implements ResourceReservationStatusService {

    private final ResourceReservationStatusRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the resource reservation status repository
     */
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
