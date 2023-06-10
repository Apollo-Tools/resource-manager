package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.entity.model.ResourceDeployment;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

/**
 * This is the implementation of the #ResourceDeploymentService.
 *
 * @author matthi-g
 */
public class ResourceDeploymentServiceImpl extends DatabaseServiceProxy<ResourceDeployment> implements ResourceDeploymentService {

    private final ResourceDeploymentRepository repository;

    /**
     * Create an instance from the resourceDeploymentRepository.
     *
     * @param repository the resource deployment repository
     */
    public ResourceDeploymentServiceImpl(ResourceDeploymentRepository repository) {
        super(repository, ResourceDeployment.class);
        this.repository = repository;
    }

    @Override
    public Future<JsonArray> findAllByDeploymentId(long deploymentId) {
        return Future
                .fromCompletionStage(repository.findAllByDeploymentId(deploymentId))
                .map(result -> {
                    ArrayList<JsonObject> objects = new ArrayList<>();
                    for (ResourceDeployment entity: result) {
                        // TODO: fix
                        if (entity instanceof ServiceDeployment) {
                            ((ServiceDeployment) entity).setService(null);
                        } else if (entity instanceof FunctionDeployment) {
                            ((FunctionDeployment) entity).setFunction(null);
                        }
                        entity.setDeployment(null);
                        entity.setResource(null);
                        objects.add(JsonObject.mapFrom(entity));
                    }
                    return new JsonArray(objects);
                });
    }

    @Override
    public Future<Void> updateTriggerUrl(long resourceDeploymentId, String triggerUrl) {
        return Future
            .fromCompletionStage(repository.updateTriggerUrl(resourceDeploymentId, triggerUrl))
            .mapEmpty();
    }

    @Override
    public Future<Void> updateSetStatusByDeploymentId(long deploymentId, DeploymentStatusValue deploymentStatusValue) {
        return Future
            .fromCompletionStage(repository
                .updateDeploymentStatusByDeploymentId(deploymentId, deploymentStatusValue))
            .mapEmpty();
    }
}
