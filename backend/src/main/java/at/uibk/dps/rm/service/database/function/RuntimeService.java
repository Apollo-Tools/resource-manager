package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.repository.function.RuntimeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

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
    static RuntimeService create(RuntimeRepository runtimeRepository) {
        return new RuntimeServiceImpl(runtimeRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static RuntimeService createProxy(Vertx vertx) {
        return new RuntimeServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Runtime.class));
    }

    /**
     * Check if a runtime exists by its name.
     *
     * @param name the name of the runtime
     * @return a Future that emits true if the function exists, else false
     */
    Future<Boolean> existsOneByName(String name);
}
