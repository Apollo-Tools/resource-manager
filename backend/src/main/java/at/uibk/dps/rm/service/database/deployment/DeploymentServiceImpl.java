package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.repository.deployment.DeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

/**
 * This is the implementation of the #DeploymentService.
 *
 * @author matthi-g
 */
public class DeploymentServiceImpl extends DatabaseServiceProxy<Deployment> implements DeploymentService {

    private final DeploymentRepository deploymentRepository;

    /**
     * Create an instance from the deploymentRepository.
     *
     * @param deploymentRepository the deployment repository
     */
    public DeploymentServiceImpl(DeploymentRepository deploymentRepository) {
        super(deploymentRepository, Deployment.class);
        this.deploymentRepository = deploymentRepository;
    }

    @Override
    public Future<JsonArray> findAllByAccountId(long accountId) {
        return Future
            .fromCompletionStage(deploymentRepository.findAllByAccountId(accountId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Deployment entity: result) {
                    entity.setCreatedBy(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<JsonObject> findOneByIdAndAccountId(long id, long accountId) {
        return Future
            .fromCompletionStage(deploymentRepository.findByIdAndAccountId(id, accountId))
            .map(result -> {
                if (result != null) {
                    result.setCreatedBy(null);
                }
                return JsonObject.mapFrom(result);
            });
    }
}
