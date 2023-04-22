package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface FunctionService extends DatabaseServiceInterface {

    @Generated
    @GenIgnore
    static FunctionService create(FunctionRepository functionRepository) {
        return new FunctionServiceImpl(functionRepository);
    }

    @Generated
    static FunctionService createProxy(Vertx vertx) {
        return new FunctionServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Function.class));
    }

    Future<JsonObject> findOne(long id);

    Future<JsonArray> findAll();

    Future<Boolean> existsOneByNameAndRuntimeIdExcludeEntity(long excludeId, String name, long runtimeId);

    Future<Boolean> existsOneByNameAndRuntimeId(String name, long runtimeId);
}
