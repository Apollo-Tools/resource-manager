package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.repository.resource.PlatformRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import io.vertx.core.json.JsonArray;

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
    static PlatformService create(PlatformRepository platformRepository, SessionManagerProvider smProvider) {
        return new PlatformServiceImpl(platformRepository, smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static PlatformService createProxy(Vertx vertx) {
        return new PlatformServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Platform.class));
    }



    /**
     * Find all platforms by resource provider.
     *
     * @param resourceProvider the resource provider
     * @param resultHandler receives the found entities as JsonArray
     */
    void findAllByResourceProvider(String resourceProvider, Handler<AsyncResult<JsonArray>> resultHandler);
}
