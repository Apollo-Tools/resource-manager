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

    public Single<JsonArray> checkFindAllByMultipleMetrics(List<String> metrics) {
        return resourceService.findAllByMultipleMetrics(metrics);
    }

    public Completable checkExistsOneByResourceType(long resourceTypeId) {
        Single<Boolean> existsOneByResourceType = resourceService.existsOneByResourceType(resourceTypeId);
        return ErrorHandler.handleUsedByOtherEntity(existsOneByResourceType).ignoreElement();
    }

    public Completable checkExistsOneAndIsNotReserved(long resourceId) {
        Single<Boolean> checkExistsOneAndIsNotReserved = resourceService.existsOneAndNotReserved(resourceId);
        return ErrorHandler.handleExistsOne(checkExistsOneAndIsNotReserved).ignoreElement();
    }
}
