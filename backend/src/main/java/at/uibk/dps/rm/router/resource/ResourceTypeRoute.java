package at.uibk.dps.rm.router.resource;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.resource.ResourceTypeChecker;
import at.uibk.dps.rm.handler.resource.ResourceTypeHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ResourceTypeRoute {
    public static void init(final RouterBuilder router, final ServiceProxyProvider serviceProxyProvider) {
        final ResourceTypeChecker resourceTypeChecker = new ResourceTypeChecker(serviceProxyProvider
            .getResourceTypeService());
        final ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        final ResourceTypeHandler resourceTypeHandler = new ResourceTypeHandler(resourceTypeChecker, resourceChecker);
        final ResultHandler resultHandler = new ResultHandler(resourceTypeHandler);

        router
            .operation("createResourceType")
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("listResourceTypes")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("getResourceType")
            .handler(resultHandler::handleFindOneRequest);

        router
            .operation("updateResourceType")
            .handler(resultHandler::handleUpdateRequest);

        router
            .operation("deleteResourceType")
            .handler(resultHandler::handleDeleteRequest);
    }
}
