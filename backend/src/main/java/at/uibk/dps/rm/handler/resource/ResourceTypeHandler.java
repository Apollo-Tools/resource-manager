package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.ValidationHandler;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;

public class ResourceTypeHandler extends ValidationHandler {

    private final ResourceChecker resourceChecker;

    public ResourceTypeHandler(ResourceTypeChecker resourceTypeChecker, ResourceChecker resourceChecker) {
        super(resourceTypeChecker);
        this.resourceChecker = resourceChecker;
    }

    @Override
    protected Completable checkDeleteEntityIsUsed(JsonObject entity) {
        return resourceChecker.checkOneUsedByResourceType(entity.getLong("type_id"));
    }
}
