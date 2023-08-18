package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

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
     * Find all resources by service level objectives.
     *
     * @param requestBody the request body
     * @return a Single that emits all found resources as JsonArray
     */
    public Single<JsonArray> checkFindAllBySLOs(JsonObject requestBody) {
        Single<JsonArray> findAllByMultipleMetrics = resourceService.findAllBySLOs(requestBody);
        return ErrorHandler.handleFindAll(findAllByMultipleMetrics);
    }

    /**
     * Find all sub resources of a main resource.
     *
     * @param resourceId the id of the resource
     * @return a Single that emits all found resources as JsonArray
     */
    public Single<JsonArray> checkFindAllSubResources(long resourceId) {
        Single<JsonArray> findAllSubResources = resourceService.findAllSubResources(resourceId);
        return ErrorHandler.handleFindAll(findAllSubResources);
    }
}
