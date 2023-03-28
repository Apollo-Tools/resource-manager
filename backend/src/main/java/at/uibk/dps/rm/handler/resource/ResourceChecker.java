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

    public Single<JsonArray> checkFindAllUnreserved() {
        Single<JsonArray> findAllUnreserved = resourceService.findAllUnreserved();
        return ErrorHandler.handleFindAll(findAllUnreserved);
    }

    public Single<JsonArray> checkFindAllByMultipleMetrics(long functionId, List<String> metrics) {
        Single<JsonArray> findAllByMultipleMetrics = resourceService.findAllByFunctionAndMultipleMetrics(functionId,
            metrics);
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
