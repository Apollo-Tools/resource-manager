package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.dto.resource.ResourceTypeEnum;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Set;

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
     * Find all resources by a list of resource ids.
     *
     * @param resourceIds the list of resource ids
     * @return a Single that emits all found resources as JsonArray
     */
    public Single<JsonArray> checkFindAllByResourceIds(List<Long> resourceIds) {
        Single<JsonArray> findAllByResourceIds = resourceService.findAllByResourceIds(resourceIds);
        return ErrorHandler.handleFindAll(findAllByResourceIds);
    }

    public Single<JsonArray> checkFindAllSubresources(long resourceId) {
        Single<JsonArray> findAllSubresources = resourceService.findAllSubresources(resourceId);
        return ErrorHandler.handleFindAll(findAllSubresources);
    }

    /**
     * Check whether all resources from the serviceResourceIds and functionResourceIds exists with
     * a suitable resource type.
     *
     * @param serviceResourceIds the list of service resource ids
     * @param functionResourceIds the list of function resource ids
     * @return a Completable if all resources exists, else  a NotFoundException gets thrown
     */
    public Completable checkExistAllByIdsAndResourceType(List<ServiceResourceIds> serviceResourceIds,
        List<FunctionResourceIds> functionResourceIds) {
        List<String> functionResourceTypes = List.of(ResourceTypeEnum.FAAS.getValue());
        List<String> serviceResourceTypes = List.of(ResourceTypeEnum.CONTAINER.getValue());

        Single<Boolean> existsAllServiceResources = Observable.fromIterable(serviceResourceIds)
            .map(ServiceResourceIds::getResourceId)
            .toList()
            .map(Set::copyOf)
            .flatMap(result -> resourceService.existsAllByIdsAndResourceTypes(result, serviceResourceTypes));

        Single<Boolean> existsAllFunctionResources =  Observable.fromIterable(functionResourceIds)
            .map(FunctionResourceIds::getResourceId)
            .toList()
            .map(Set::copyOf)
            .flatMap(result -> resourceService.existsAllByIdsAndResourceTypes(result, functionResourceTypes));

        Single<Boolean> existsAll = existsAllFunctionResources
            .flatMap(existsAllSR -> existsAllServiceResources.map(existsAllFR -> existsAllSR && existsAllFR));

        return ErrorHandler.handleExistsOne(existsAll).ignoreElement();
    }
}
