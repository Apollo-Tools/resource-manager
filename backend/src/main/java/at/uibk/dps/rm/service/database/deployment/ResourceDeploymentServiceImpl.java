package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ResourceDeployment;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import org.hibernate.reactive.stage.Stage;

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
    public Future<Void> updateSetStatusByDeploymentId(long deploymentId, DeploymentStatusValue deploymentStatusValue) {
        CompletionStage<Integer> updateStatus = withTransaction(session ->
            repository.updateDeploymentStatusByDeploymentId(session, deploymentId, deploymentStatusValue));
        return Future.fromCompletionStage(updateStatus)
            .mapEmpty();
    }
}
