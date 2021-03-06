package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.resource.ResourceTypeService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class ResourceTypeChecker extends EntityChecker {

    private final ResourceTypeService resourceTypeService;

    public ResourceTypeChecker(ResourceTypeService resourceTypeService) {
        super(resourceTypeService);
        this.resourceTypeService = resourceTypeService;
    }

    @Override
    public Completable checkForDuplicateEntity(JsonObject entity) {
        Single<Boolean> existsOneByResourceType = resourceTypeService.existsOneByResourceType(entity.getString("resource_type"));
        return ErrorHandler.handleDuplicates(existsOneByResourceType).ignoreElement();
    }

    @Override
    public Single<JsonObject> checkUpdateNoDuplicate(JsonObject requestBody, JsonObject entity) {
        if (requestBody.containsKey("resource_type")) {
            return this.checkForDuplicateEntity(requestBody)
                .andThen(Single.just(entity));
        }
        return Single.just(entity);
    }
}
