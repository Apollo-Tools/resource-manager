package at.uibk.dps.rm.router.resource;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.resource.ResourceSLOInputHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialize the resource slo route.
 *
 * @author matthi-g
 */
public class ResourceSLORoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        ResultHandler resultHandler = new ResultHandler(null);

        router
            .operation("listResourcesBySLOs")
            .handler(ResourceSLOInputHandler::validateGetResourcesBySLOsRequest)
            .handler(rc -> {
                JsonObject requestBody = rc.body().asJsonObject();
                resultHandler.handleFindAllRequest(rc, resourceChecker.checkFindAllBySLOs(requestBody));
            });
    }
}
