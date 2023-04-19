package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionResourceService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Implements methods to perform CRUD operations on the function_resource entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class FunctionResourceChecker  extends EntityChecker {
    private final FunctionResourceService functionResourceService;

    /**
     * Create an instance from the functionResourceService.
     *
     * @param functionResourceService the function resource service
     */
    public FunctionResourceChecker(FunctionResourceService functionResourceService) {
        super(functionResourceService);
        this.functionResourceService = functionResourceService;
    }

    /**
     * Submit the deletion of a function resource by its function and resource
     *
     * @param functionId the id of the function
     * @param resourceId the id of the resource
     * @return a Completable
     */
    public Completable submitDeleteFunctionResource(long functionId, long resourceId) {
        return functionResourceService.deleteByFunctionAndResource(functionId, resourceId);
    }

    /**
     * Find a function resource by its functionId and resourceId.
     *
     * @param functionId the id of the function
     * @param resourceId the id of the resource
     * @return a Single that emits the found function resource as JsonObject
     */
    public Single<JsonObject> checkFindOneByFunctionAndResource(long functionId, long resourceId) {
        Single<JsonObject> findOneByFunctionAndResource = functionResourceService
            .findOneByFunctionAndResource(functionId, resourceId);
        return ErrorHandler.handleFindOne(findOneByFunctionAndResource);
    }

    /**
     * Find all function resources by a reservation.
     *
     * @param reservationId the id of the reservation
     * @return a Single that emits the found function resource as JsonArray
     */
    public Single<JsonArray> checkFindAllByReservationId(long reservationId) {
        Single<JsonArray> findAllByReservationId = functionResourceService.findAllByReservationId(reservationId);
        return ErrorHandler.handleFindAll(findAllByReservationId);
    }

    /**
     * Check if the creation of a function resource would create a duplicate.
     *
     * @param functionId the id of the function
     * @param resourceId the id of the resource
     * @return a Completable
     */
    public Completable checkForDuplicateByFunctionAndResource(long functionId, long resourceId) {
        Single<Boolean> existsOneByFunctionAndResource = functionResourceService
            .existsOneByFunctionAndResource(functionId, resourceId);
        return ErrorHandler.handleDuplicates(existsOneByFunctionAndResource).ignoreElement();
    }

    /**
     * Check if function resource exists.
     *
     * @param functionId the id of the function
     * @param resourceId the id of the resource
     * @return a Completable
     */
    public Completable checkExistsByFunctionAndResource(long functionId, long resourceId) {
        Single<Boolean> existsOneByFunctionAndResource = functionResourceService.existsOneByFunctionAndResource(functionId, resourceId);
        return ErrorHandler.handleExistsOne(existsOneByFunctionAndResource).ignoreElement();
    }
}
