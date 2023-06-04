package at.uibk.dps.rm.router.resource;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resource.PlatformChecker;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.resource.ResourceHandler;
import at.uibk.dps.rm.handler.resourceprovider.RegionChecker;
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
        ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        PlatformChecker platformChecker = new PlatformChecker(serviceProxyProvider.getPlatformService());
        RegionChecker regionChecker = new RegionChecker(serviceProxyProvider.getRegionService());
        ResourceHandler resourceHandler = new ResourceHandler(resourceChecker, platformChecker, regionChecker);
        ResultHandler resultHandler = new ResultHandler(resourceHandler);

        router
            .operation("createResource")
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("listResources")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("getResource")
            .handler(resultHandler::handleFindOneRequest);

        router
            .operation("deleteResource")
            .handler(resultHandler::handleDeleteRequest);
    }
}
