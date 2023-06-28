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
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

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
    public ResourceDeploymentServiceImpl(ResourceDeploymentRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, ResourceDeployment.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<JsonArray> findAllByDeploymentId(long deploymentId) {
        CompletionStage<List<ResourceDeployment>> findAll = withSession(session ->
            repository.findAllByDeploymentId(session, deploymentId));
        return Future.fromCompletionStage(findAll)
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
        CompletionStage<Integer> updateTriggerUrl = withTransaction(session ->
            repository.updateTriggerUrl(session, resourceDeploymentId, triggerUrl));
        return Future.fromCompletionStage(updateTriggerUrl)
            .mapEmpty();
    }

    @Override
    public Future<Void> updateSetStatusByDeploymentId(long deploymentId, DeploymentStatusValue deploymentStatusValue) {
        CompletionStage<Integer> updateStatus = withTransaction(session ->
            repository.updateDeploymentStatusByDeploymentId(session, deploymentId, deploymentStatusValue));
        return Future.fromCompletionStage(updateStatus)
            .mapEmpty();
    }
}
