package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.repository.deployment.ServiceDeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.Objects;

/**
 * This is the implementation of the #ServiceDeploymentService.
 *
 * @author matthi-g
 */
public class ServiceDeploymentServiceImpl extends DatabaseServiceProxy<ServiceDeployment>
    implements ServiceDeploymentService {

    private final ServiceDeploymentRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the service deployment repository
     */
    public ServiceDeploymentServiceImpl(ServiceDeploymentRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, ServiceDeployment.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<JsonArray> findAllByDeploymentId(long deploymentId) {
        return Future
            .fromCompletionStage(repository.findAllByDeploymentId(deploymentId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (ServiceDeployment entity: result) {
                    entity.getResource().getRegion().getResourceProvider().setProviderPlatforms(null);
                    entity.setDeployment(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Boolean> existsReadyForContainerStartupAndTermination(long deploymentId,
        long resourceDeploymentId, long accountId) {
        return Future.fromCompletionStage(repository.findOneByDeploymentStatus(deploymentId, resourceDeploymentId,
                accountId, DeploymentStatusValue.DEPLOYED))
            .map(Objects::nonNull);
    }
}
