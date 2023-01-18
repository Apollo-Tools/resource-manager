package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.repository.FunctionRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface FunctionService extends ServiceInterface {
    @GenIgnore
    static FunctionService create(FunctionRepository functionRepository) {
        return new FunctionServiceImpl(functionRepository);
    }

    static FunctionService createProxy(Vertx vertx, String address) {
        return new FunctionServiceVertxEBProxy(vertx, address);
    }
}
