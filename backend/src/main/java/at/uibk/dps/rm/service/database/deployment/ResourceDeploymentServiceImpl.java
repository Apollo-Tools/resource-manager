package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ResourceDeployment;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

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
    public ResourceDeploymentServiceImpl(ResourceDeploymentRepository repository, SessionManagerProvider smProvider) {
        super(repository, ResourceDeployment.class, smProvider);
        this.repository = repository;
    }

    @Override
    public void updateStatusByDeploymentId(long deploymentId, DeploymentStatusValue deploymentStatusValue,
            Handler<AsyncResult<Void>> resultHandler) {
        Completable updateStatus = smProvider.withTransactionCompletable(sm ->
            repository.updateDeploymentStatusByDeploymentId(sm, deploymentId, deploymentStatusValue));
        RxVertxHandler.handleSession(updateStatus, resultHandler);
    }
}
