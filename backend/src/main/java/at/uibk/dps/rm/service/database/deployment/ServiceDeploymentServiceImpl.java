package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.deployment.ServiceDeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import io.vertx.core.json.JsonArray;

import java.util.List;

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

    public void findAllForServiceOperation(List<Long> resourceDeploymentIds, long accountId, long deploymentId,
            Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<ServiceDeployment>> findAll = smProvider.withTransactionSingle(sm -> repository
            .findAllByIdsAccountIdAndDeploymentId(sm, resourceDeploymentIds, accountId, deploymentId)
            .flatMap(serviceDeployments ->  {
                if (serviceDeployments.isEmpty()) {
                    return Single.just(List.of());
                }
                return Observable.fromIterable(serviceDeployments)
                    .flatMapSingle(serviceDeployment -> {
                        DeploymentStatusValue status = DeploymentStatusValue
                            .fromDeploymentStatus(serviceDeployment.getStatus());
                        if (!status.equals(DeploymentStatusValue.DEPLOYED)) {
                            return Single.error(new BadInputException("Service Deployment is not ready for " +
                                "startup/shutdown"));
                        } else {
                            return sm.fetch(serviceDeployment.getService().getEnvVars())
                                .flatMap(res -> sm.fetch(serviceDeployment.getService().getVolumeMounts()))
                                .map(res -> serviceDeployment);
                        }
                    })
                    .toList();
            })
        );
        RxVertxHandler.handleSession(findAll.map(this::mapResultListToJsonArray), resultHandler);
    }
}
