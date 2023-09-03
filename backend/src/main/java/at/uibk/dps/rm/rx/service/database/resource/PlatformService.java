package at.uibk.dps.rm.rx.service.database.resource;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.rx.repository.resource.PlatformRepository;
import at.uibk.dps.rm.rx.service.ServiceProxyAddress;
import at.uibk.dps.rm.rx.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import org.hibernate.reactive.stage.Stage;

/**
 * The interface of the service proxy for the platform entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface PlatformService  extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static PlatformService create(PlatformRepository platformRepository, Stage.SessionFactory sessionFactory) {
        return new PlatformServiceImpl(platformRepository, sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static PlatformService createProxy(Vertx vertx) {
        return new PlatformServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Platform.class));
    }

}
