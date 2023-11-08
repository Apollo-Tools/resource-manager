package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.entity.model.FunctionDeploymentExecTime;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.deployment.FunctionDeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * This is the implementation of the {@link FunctionDeploymentService}.
 *
 * @author matthi-g
 */
public class FunctionDeploymentServiceImpl extends DatabaseServiceProxy<FunctionDeployment>
        implements FunctionDeploymentService {

    private final FunctionDeploymentRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the service deployment repository
     */
    public FunctionDeploymentServiceImpl(FunctionDeploymentRepository repository, SessionManagerProvider smProvider) {
        super(repository, FunctionDeployment.class, smProvider);
        this.repository = repository;
    }

    public void findOneForInvocation(long id, long accountId, Handler<AsyncResult<JsonObject>> resultHandler) {
        Single<FunctionDeployment> findOne = smProvider.withTransactionSingle(sm -> repository
            .findByIdAndAccountId(sm, id, accountId)
            .switchIfEmpty(Single.error(new NotFoundException(FunctionDeployment.class)))
            .flatMap(functionDeployment -> {
                DeploymentStatusValue status = DeploymentStatusValue
                    .fromDeploymentStatus(functionDeployment.getStatus());
                if (!status.equals(DeploymentStatusValue.DEPLOYED) || functionDeployment.getDirectTriggerUrl()
                    .isBlank()) {
                    return Single.error(new BadInputException("Function Deployment is not ready for invocation"));
                } else {
                    return Single.just(functionDeployment);
                }
            })
        );
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    public void saveExecTime(long id, int execTimeMs, String requestBody, Handler<AsyncResult<Void>> resultHandler) {
        Completable saveExecTime = smProvider.withTransactionCompletable(sm ->  sm
            .find(FunctionDeployment.class, id)
            .switchIfEmpty(Single.error(new NotFoundException(FunctionDeployment.class)))
            .flatMapCompletable(functionDeployment -> {
                FunctionDeploymentExecTime execTime = new FunctionDeploymentExecTime();
                execTime.setFunctionDeployment(functionDeployment);
                execTime.setRequest_body(requestBody);
                execTime.setExecTimeMs(execTimeMs);
                return sm.persist(execTime).ignoreElement();
            })
        );
        RxVertxHandler.handleSession(saveExecTime, resultHandler);
    }
}
