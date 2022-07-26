package at.uibk.dps.rm.handler.Resource;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.resource.ResourceTypeService;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;

public class ResourceTypeHandler extends ValidationHandler {

    private final ResourceChecker resourceChecker;

    public ResourceTypeHandler(ResourceTypeService resourceTypeService, ResourceService resourceService) {
        super(new ResourceTypeChecker(resourceTypeService));
        resourceChecker = new ResourceChecker(resourceService);
    }

    @Override
    protected Completable checkDeleteEntityIsUsed(JsonObject entity) {
        return resourceChecker.checkExistsOneByResourceType(entity.getLong("type_id"));
    }
}
