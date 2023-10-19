package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Environment;
import at.uibk.dps.rm.repository.resourceprovider.EnvironmentRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

/**
 * The interface of the service proxy for the environment entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface EnvironmentService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static EnvironmentService create(EnvironmentRepository environmentRepository, SessionManagerProvider smProvider) {
        return new EnvironmentServiceImpl(environmentRepository, smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static EnvironmentService createProxy(Vertx vertx) {
        return new EnvironmentServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Environment.class));
    }
}
