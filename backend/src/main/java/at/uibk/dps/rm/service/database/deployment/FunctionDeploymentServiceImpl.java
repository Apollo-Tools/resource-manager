package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.repository.deployment.FunctionDeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

/**
 * This is the implementation of the #FunctionDeploymentService.
 *
 * @author matthi-g
 */
public class FunctionDeploymentServiceImpl extends DatabaseServiceProxy<FunctionDeployment> implements
    FunctionDeploymentService {

    private final FunctionDeploymentRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the function deployment repository
     */
    public FunctionDeploymentServiceImpl(FunctionDeploymentRepository repository) {
        super(repository, FunctionDeployment.class);
        this.repository = repository;
    }

    @Override
    public Future<JsonArray> findAllByDeploymentId(long deploymentId) {
        return Future
            .fromCompletionStage(repository.findAllByDeploymentId(deploymentId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (FunctionDeployment entity: result) {
                    entity.setDeployment(null);
                    entity.getResource().getRegion().getResourceProvider().setProviderPlatforms(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }
}
