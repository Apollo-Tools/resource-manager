package at.uibk.dps.rm.router.resource;

import at.uibk.dps.rm.handler.resource.ResourceSLOInputHandler;
import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resource.ResourceHandler;
import at.uibk.dps.rm.handler.resource.ResourceInputHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the resource route.
 *
 * @author matthi-g
 */
public class ResourceRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ResourceHandler resourceHandler = new ResourceHandler(serviceProxyProvider.getResourceService(),
            serviceProxyProvider.getMetricService(), serviceProxyProvider.getMetricQueryService());
        ResultHandler resultHandler = new ResultHandler(resourceHandler);

        router
            .operation("createResource")
            .handler(ResourceInputHandler::validateAddResourceRequest)
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("listResources")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("listSubresources")
            .handler(rc -> resultHandler.handleFindAllRequest(rc,
                resourceHandler.getAllSubResourcesByMainResource(rc)));

        router
            .operation("listResourcesBySLOs")
            .handler(ResourceSLOInputHandler::validateGetResourcesBySLOsRequest)
            .handler(rc -> resultHandler.handleFindAllRequest(rc, resourceHandler.getAllBySLOs(rc)));

        router.operation("listLockedResourcesByDeployment")
            .handler(rc -> resultHandler.handleFindAllRequest(rc, resourceHandler.getAllLockedByDeployment(rc)));

        router
            .operation("getResource")
            .handler(resultHandler::handleFindOneRequest);

        router
            .operation("updateResource")
            .handler(resultHandler::handleUpdateRequest);

        router
            .operation("deleteResource")
            .handler(resultHandler::handleDeleteRequest);
    }
}
