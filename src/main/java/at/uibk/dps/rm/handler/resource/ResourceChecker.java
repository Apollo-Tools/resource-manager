package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.resource.ResourceService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class ResourceChecker extends EntityChecker {

    private final ResourceService resourceService;

    public ResourceChecker(ResourceService resourceService) {
        super(resourceService);
        this.resourceService = resourceService;
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
