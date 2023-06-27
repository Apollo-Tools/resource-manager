package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.repository.function.RuntimeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import org.hibernate.reactive.stage.Stage;

/**
 * The interface of the service proxy for the runtime entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface RuntimeService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static RuntimeService create(RuntimeRepository runtimeRepository, Stage.SessionFactory sessionFactory) {
        return new RuntimeServiceImpl(runtimeRepository, sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static RuntimeService createProxy(Vertx vertx) {
        return new RuntimeServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Runtime.class));
    }
}
