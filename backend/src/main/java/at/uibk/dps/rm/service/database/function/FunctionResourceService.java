package at.uibk.dps.rm.service.database.function;


import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.repository.function.FunctionResourceRepository;
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
public interface FunctionResourceService extends DatabaseServiceInterface {

    @Generated
    @GenIgnore
    static FunctionResourceService create(FunctionResourceRepository functionResourceRepository) {
        return new FunctionResourceServiceImpl(functionResourceRepository);
    }

    @Generated
    static FunctionResourceService createProxy(Vertx vertx) {
        return new FunctionResourceServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(FunctionResource.class));
    }

    Future<JsonObject> findOneByFunctionAndResource(long functionId, long resourceId);

    Future<JsonArray> findAllByReservationId(long reservationId);

    Future<Boolean> existsOneByFunctionAndResource(long functionId, long resourceId);

    Future<Void> deleteByFunctionAndResource(long functionId, long resourceId);
}
