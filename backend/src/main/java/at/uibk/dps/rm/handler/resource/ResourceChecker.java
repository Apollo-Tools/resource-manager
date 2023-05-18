package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.reservation.ServiceResourceIds;
import at.uibk.dps.rm.entity.dto.resource.ResourceTypeEnum;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

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
     * Find all resources by function and service level objectives.
     *
     * @param metrics the list of metrics
     * @param regionIds the list of region ids
     * @param providerIds the list of provider ids
     * @param resourceTypeIds the list of resource type ids
     * @return a Single that emits all found resources as JsonArray
     */
    public Single<JsonArray> checkFindAllBySLOs(List<String> metrics,
        List<Long> regionIds, List<Long> providerIds, List<Long> resourceTypeIds) {
        Single<JsonArray> findAllByMultipleMetrics = resourceService.findAllBySLOs(metrics, regionIds, providerIds,
                resourceTypeIds);
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
     * Find all resources by ensemble.
     *
     * @param ensembleId the id of the ensemble
     * @return a Single that emits all found resources as JsonArray
     */
    public Single<JsonArray> checkFindAllByEnsemble(long ensembleId) {
        Single<JsonArray> findAllByEnsemble = resourceService.findAllByEnsembleId(ensembleId);
        return ErrorHandler.handleFindAll(findAllByEnsemble);
    }

    /**
     * Find all resources by a list of resource ids.
     *
     * @param resourceIds the list of resource ids
     * @return a Single that emits all found resources as JsonArray
     */
    public Single<JsonArray> checkFindAllByResourceIds(List<Long> resourceIds) {
        Single<JsonArray> findAllByResourceIds = resourceService.findAllByResourceId(resourceIds);
        return ErrorHandler.handleFindAll(findAllByResourceIds);
    }

    /**
     * Check if at least one resource is used by resource type.
     *
     * @param resourceTypeId the id of the resource type
     * @return a Completable the resource is not used by another entity, else a
     * UsedByOtherEntityException gets thrown
     */
    public Completable checkOneUsedByResourceType(long resourceTypeId) {
        Single<Boolean> existsOneByResourceType = resourceService.existsOneByResourceType(resourceTypeId);
        return ErrorHandler.handleUsedByOtherEntity(existsOneByResourceType).ignoreElement();
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
        List<String> functionResourceTypes = List.of(ResourceTypeEnum.EDGE.getValue(), ResourceTypeEnum.FAAS.getValue(),
            ResourceTypeEnum.VM.getValue());
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
