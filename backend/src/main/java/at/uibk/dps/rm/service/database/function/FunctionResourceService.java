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

/**
 * The interface of the service proxy for the function_resource entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface FunctionResourceService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static FunctionResourceService create(FunctionResourceRepository functionResourceRepository) {
        return new FunctionResourceServiceImpl(functionResourceRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static FunctionResourceService createProxy(Vertx vertx) {
        return new FunctionResourceServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(FunctionResource.class));
    }

    /**
     * Find a function resource by its function and resource.
     *
     * @param functionId the id of the function
     * @param resourceId the id of the resource
     * @return a Future that emits the function resource as JsonObject if it exists, else null
     */
    Future<JsonObject> findOneByFunctionAndResource(long functionId, long resourceId);

    /**
     * Find all function resources that belong to the reservation.
     *
     * @param reservationId the id of the reservation
     * @return a Future that emits all function resources as JsonArray
     */
    Future<JsonArray> findAllByReservationId(long reservationId);

    /**
     * Check if a function resource exists by its function and resource.
     *
     * @param functionId the id of the function
     * @param resourceId the id of the resource
     * @return a Future that emits true if the function resource exists, else false
     */
    Future<Boolean> existsOneByFunctionAndResource(long functionId, long resourceId);

    /**
     * Delete a function resource by its function and resource.
     *
     * @param functionId the id of the function
     * @param resourceId the id of the resource
     * @return an empty Future
     */
    Future<Void> deleteByFunctionAndResource(long functionId, long resourceId);
}
