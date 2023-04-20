package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

import java.util.List;

/**
 * Implements methods to perform CRUD operations on the resource entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class ResourceChecker extends EntityChecker {

    private final ResourceService resourceService;

    /**
     * Create an instance from the resourceService.
     *
     * @param resourceService the resource service
     */
    public ResourceChecker(ResourceService resourceService) {
        super(resourceService);
        this.resourceService = resourceService;
    }

    /**
     * Find all resources by function and service level objectives.
     *
     * @param functionId the id of the function
     * @param metrics the list of metrics
     * @param regions the list of regions
     * @param providerIds the list of provider ids
     * @param resourceTypeIds the list of resource type ids
     * @return a Single that emits all found resources as JsonArray
     */
    public Single<JsonArray> checkFindAllBySLOs(long functionId, List<String> metrics,
        List<String> regions, List<Long> providerIds, List<Long> resourceTypeIds) {
        Single<JsonArray> findAllByMultipleMetrics = resourceService.findAllBySLOs(functionId,
            metrics, regions, providerIds, resourceTypeIds);
        return ErrorHandler.handleFindAll(findAllByMultipleMetrics);
    }

    /**
     * Find all resources by function.
     *
     * @param functionId the id of the function
     * @return a Single that emits all found resources as JsonArray
     */
    public Single<JsonArray> checkFindAllByFunction(long functionId) {
        Single<JsonArray> findAllByFunction = resourceService.findAllByFunctionId(functionId);
        return ErrorHandler.handleFindAll(findAllByFunction);
    }

    /**
     * Check if at least one resource is used by resource type.
     *
     * @param resourceTypeId the id of the resource type
     * @return a Completable
     */
    public Completable checkOneUsedByResourceType(long resourceTypeId) {
        Single<Boolean> existsOneByResourceType = resourceService.existsOneByResourceType(resourceTypeId);
        return ErrorHandler.handleUsedByOtherEntity(existsOneByResourceType).ignoreElement();
    }
}
