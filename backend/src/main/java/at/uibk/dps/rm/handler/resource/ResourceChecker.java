package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

import java.util.List;

public class ResourceChecker extends EntityChecker {

    private final ResourceService resourceService;

    public ResourceChecker(ResourceService resourceService) {
        super(resourceService);
        this.resourceService = resourceService;
    }

    public Single<JsonArray> checkFindAllBySLOs(long functionId, List<String> metrics, List<String> regions,
                                                List<Long> providerIds, List<Long> resourceTypeIds) {
        Single<JsonArray> findAllByMultipleMetrics = resourceService.findAllBySLOs(functionId,
            metrics, regions, providerIds, resourceTypeIds);
        return ErrorHandler.handleFindAll(findAllByMultipleMetrics);
    }

    public Single<JsonArray> checkFindAllByFunction(long id) {
        Single<JsonArray> findAllByFunction = resourceService.findAllByFunctionId(id);
        return ErrorHandler.handleFindAll(findAllByFunction);
    }

    public Completable checkOneUsedByResourceType(long resourceTypeId) {
        Single<Boolean> existsOneByResourceType = resourceService.existsOneByResourceType(resourceTypeId);
        return ErrorHandler.handleUsedByOtherEntity(existsOneByResourceType).ignoreElement();
    }
}
