package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.repository.log.LogRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

/**
 * The interface of the service proxy for the log entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface LogService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static LogService create(LogRepository logRepository, SessionManagerProvider smProvider) {
        return new LogServiceImpl(logRepository, smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static LogService createProxy(Vertx vertx) {
        return new LogServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Log.class));
    }

    /**
     * Find all logs that belong to a deployment and creator account.
     *
     * @param deploymentId the id of the deployment
     * @param accountId the id of the creator account
     * @param resultHandler receives the found entities as JsonArray
     */
    void findAllByDeploymentIdAndAccountId(long deploymentId, long accountId,
        Handler<AsyncResult<JsonArray>> resultHandler);
}
