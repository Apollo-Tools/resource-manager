package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.deployment.ServiceDeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import io.vertx.core.json.JsonObject;

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
    public ServiceDeploymentServiceImpl(ServiceDeploymentRepository repository, SessionManagerProvider smProvider) {
        super(repository, ServiceDeployment.class, smProvider);
        this.repository = repository;
    }

    @Override
    public void findOneForDeploymentAndTermination(long resourceDeploymentId, long accountId,
            Handler<AsyncResult<JsonObject>> resultHandler) {
        Single<ServiceDeployment> findOne = smProvider.withTransactionSingle(sm -> repository
            .findByIdAndAccountIdAndFetch(sm, resourceDeploymentId, accountId)
            .switchIfEmpty(Single.error(new NotFoundException(ServiceDeployment.class)))
            .map(serviceDeployment ->  {
                DeploymentStatusValue status = DeploymentStatusValue
                    .fromDeploymentStatus(serviceDeployment.getStatus());
                if (!status.equals(DeploymentStatusValue.DEPLOYED)) {
                    throw new BadInputException("Service Deployment is not ready for startup/termination");
                }
                return serviceDeployment;
            })
        );
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }
}
