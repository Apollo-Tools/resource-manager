package at.uibk.dps.rm.service.database.function;


import at.uibk.dps.rm.repository.FunctionResourceRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface FunctionResourceService extends ServiceInterface {
    @GenIgnore
    static FunctionResourceService create(FunctionResourceRepository functionResourceRepository) {
        return new FunctionResourceServiceImpl(functionResourceRepository);
    }

    static FunctionResourceService createProxy(Vertx vertx, String address) {
        return new FunctionResourceServiceVertxEBProxy(vertx, address);
    }
}
