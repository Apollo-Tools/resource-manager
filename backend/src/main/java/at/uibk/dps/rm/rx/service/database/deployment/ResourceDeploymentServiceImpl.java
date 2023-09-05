package at.uibk.dps.rm.rx.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ResourceDeployment;
import at.uibk.dps.rm.rx.repository.deployment.ResourceDeploymentRepository;
import at.uibk.dps.rm.rx.service.database.DatabaseServiceProxy;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.hibernate.reactive.stage.Stage;

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
    public void updateStatusByDeploymentId(long deploymentId, DeploymentStatusValue deploymentStatusValue,
            Handler<AsyncResult<Void>> resultHandler) {
        Completable updateStatus = withTransactionCompletable(sessionManager ->
            repository.updateDeploymentStatusByDeploymentId(sessionManager, deploymentId, deploymentStatusValue));
        handleSession(updateStatus, resultHandler);
    }
}
