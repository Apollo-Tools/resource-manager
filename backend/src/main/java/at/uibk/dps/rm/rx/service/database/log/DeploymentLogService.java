package at.uibk.dps.rm.rx.service.database.log;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.DeploymentLog;
import at.uibk.dps.rm.rx.repository.log.DeploymentLogRepository;
import at.uibk.dps.rm.rx.service.ServiceProxyAddress;
import at.uibk.dps.rm.rx.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import org.hibernate.reactive.stage.Stage;

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
    static DeploymentLogService create(DeploymentLogRepository repository, Stage.SessionFactory sessionFactory) {
        return new DeploymentLogServiceImpl(repository, sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static DeploymentLogService createProxy(Vertx vertx) {
        return new DeploymentLogServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(DeploymentLog.class));
    }
}
