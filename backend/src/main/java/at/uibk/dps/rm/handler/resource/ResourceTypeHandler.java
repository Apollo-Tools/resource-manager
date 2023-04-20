package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.ValidationHandler;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;

/**
 * Processes the http requests that concern the resource_type entity.
 *
 * @author matthi-g
 */
public class ResourceTypeHandler extends ValidationHandler {

    private final ResourceChecker resourceChecker;

    /**
     * Create an instance from the resourceTypeChecker and resourceChecker.
     *
     * @param resourceTypeChecker the resource type checker
     * @param resourceChecker the resource checker
     */
    public ResourceTypeHandler(ResourceTypeChecker resourceTypeChecker, ResourceChecker resourceChecker) {
        super(resourceTypeChecker);
        this.resourceChecker = resourceChecker;
    }

    @Override
    protected Completable checkDeleteEntityIsUsed(JsonObject entity) {
        return resourceChecker.checkOneUsedByResourceType(entity.getLong("type_id"));
    }
}
