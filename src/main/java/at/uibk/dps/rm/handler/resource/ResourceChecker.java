package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.resourcemanager.ResourceManagerService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class ResourceChecker extends EntityChecker {

    private final ResourceService resourceService;

    private final ResourceManagerService resourceManagerService;

    public ResourceChecker(ResourceService resourceService) {
        super(resourceService);
        this.resourceService = resourceService;
        this.resourceManagerService = null;
    }

    public ResourceChecker(ResourceService resourceService, ResourceManagerService resourceManagerService) {
        super(resourceService);
        this.resourceService = resourceService;
        this.resourceManagerService = resourceManagerService;
    }

    public Single<JsonObject> checkFindOne(long id) {
        if (resourceManagerService == null) {
            throw new IllegalStateException();
        }
        Single<JsonObject> findOneById = resourceManagerService.getOne(id);
        return ErrorHandler.handleFindOne(findOneById);
    }

    public Single<JsonArray> checkFindAllByMultipleMetrics(List<String> metrics) {
        return resourceService.findAllByMultipleMetrics(metrics);
    }

    @Override
    public Completable checkForDuplicateEntity(JsonObject entity) {
        Single<Boolean> existsOneByUrl = resourceService.existsOneByUrl(entity.getString("url"));
        return ErrorHandler.handleDuplicates(existsOneByUrl).ignoreElement();
    }

    public Completable checkExistsOneByResourceType(long resourceTypeId) {
        Single<Boolean> existsOneByResourceType = resourceService.existsOneByResourceType(resourceTypeId);
        return ErrorHandler.handleUsedByOtherEntity(existsOneByResourceType).ignoreElement();
    }

    @Override
    public Single<JsonObject> checkUpdateNoDuplicate(JsonObject requestBody, JsonObject entity) {
        if (requestBody.containsKey("url")) {
            return this.checkForDuplicateEntity(requestBody)
                .andThen(Single.just(entity));
        }
        return Single.just(entity);
    }
}
