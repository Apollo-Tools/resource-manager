package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.repository.deployment.ServiceDeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.hibernate.reactive.stage.Stage;

/**
 * This is the implementation of the {@link ServiceDeploymentService}.
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
    public void existsReadyForContainerStartupAndTermination(long deploymentId, long resourceDeploymentId,
            long accountId, Handler<AsyncResult<Boolean>> resultHandler) {
        Single<Boolean> existsOne = SessionManager.withTransactionSingle(sessionFactory, sm -> repository
            .countByDeploymentStatus(sm, deploymentId, resourceDeploymentId, accountId,
                DeploymentStatusValue.DEPLOYED)
            .map(count -> count == 1)
        );
        RxVertxHandler.handleSession(existsOne, resultHandler);
    }
}
