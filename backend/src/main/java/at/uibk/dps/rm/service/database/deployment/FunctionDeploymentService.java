package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.repository.deployment.FunctionDeploymentRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * The interface of the service proxy for the function_deployment entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface FunctionDeploymentService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static FunctionDeploymentService create(FunctionDeploymentRepository repository, SessionManagerProvider smProvider) {
        return new FunctionDeploymentServiceImpl(repository, smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static FunctionDeploymentService createProxy(Vertx vertx) {
        return new FunctionDeploymentServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(FunctionDeployment.class));
    }

    /**
     * Find function deployment to be invoked by id and account id.
     *
     * @param id the id of the entity
     * @param accountId the id of the creator
     * @param resultHandler receives the entity as JsonObject if it exists else an exception
     */
    void findOneForInvocation(long id, long accountId, Handler<AsyncResult<JsonObject>> resultHandler);

    /**
     * Save the execution time of a function deployment.
     *
     * @param id the id of the function deployment
     * @param execTimeMs the execution time
     * @param resultHandler receives nothing if the update was successful else an error
     */
    void saveExecTime(long id, int execTimeMs, String requestBody, Handler<AsyncResult<Void>> resultHandler);
}
