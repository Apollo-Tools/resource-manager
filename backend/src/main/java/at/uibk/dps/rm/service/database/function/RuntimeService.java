package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.repository.RuntimeRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface RuntimeService extends ServiceInterface {
    @GenIgnore
    static RuntimeService create(RuntimeRepository runtimeRepository) {
        return new RuntimeServiceImpl(runtimeRepository);
    }

    static RuntimeService createProxy(Vertx vertx, String address) {
        return new RuntimeServiceVertxEBProxy(vertx, address);
    }
}
