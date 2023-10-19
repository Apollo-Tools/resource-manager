package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.DeploymentLog;
import at.uibk.dps.rm.repository.log.DeploymentLogRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

/**
 * The interface of the service proxy for the deployment_log entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface DeploymentLogService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static DeploymentLogService create(DeploymentLogRepository repository, SessionManagerProvider smProvider) {
        return new DeploymentLogServiceImpl(repository, smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static DeploymentLogService createProxy(Vertx vertx) {
        return new DeploymentLogServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(DeploymentLog.class));
    }
}
