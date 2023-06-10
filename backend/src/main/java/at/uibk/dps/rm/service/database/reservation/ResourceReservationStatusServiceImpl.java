package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.model.ResourceDeploymentStatus;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentStatusRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * This is the implementation of the #ResourceReservationStatusService.
 *
 * @author matthi-g
 */
public class ResourceReservationStatusServiceImpl  extends DatabaseServiceProxy<ResourceDeploymentStatus>
    implements ResourceReservationStatusService {

    private final ResourceDeploymentStatusRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the resource reservation status repository
     */
    public ResourceReservationStatusServiceImpl(ResourceDeploymentStatusRepository repository) {
        super(repository, ResourceDeploymentStatus.class);
        this.repository = repository;
    }

    @Override
    public Future<JsonObject> findOneByStatusValue(String statusValue) {
        return Future
            .fromCompletionStage(repository.findOneByStatusValue(statusValue))
            .map(JsonObject::mapFrom);
    }
}
