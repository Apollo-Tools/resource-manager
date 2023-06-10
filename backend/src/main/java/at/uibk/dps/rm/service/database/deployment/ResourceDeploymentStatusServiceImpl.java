package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.model.ResourceDeploymentStatus;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentStatusRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * This is the implementation of the #ResourceDeploymentStatusService.
 *
 * @author matthi-g
 */
public class ResourceDeploymentStatusServiceImpl extends DatabaseServiceProxy<ResourceDeploymentStatus>
    implements ResourceDeploymentStatusService {

    private final ResourceDeploymentStatusRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the resource deployment status repository
     */
    public ResourceDeploymentStatusServiceImpl(ResourceDeploymentStatusRepository repository) {
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
