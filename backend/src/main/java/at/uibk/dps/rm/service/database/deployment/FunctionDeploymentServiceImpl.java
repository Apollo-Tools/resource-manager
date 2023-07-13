package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.repository.deployment.FunctionDeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

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
    public FunctionDeploymentServiceImpl(FunctionDeploymentRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, FunctionDeployment.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<JsonArray> findAllByDeploymentId(long deploymentId) {
        CompletionStage<List<FunctionDeployment>> findAll = withSession(session ->
            repository.findAllByDeploymentId(session, deploymentId));
        return Future.fromCompletionStage(findAll)
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
