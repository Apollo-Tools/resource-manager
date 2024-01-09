package at.uibk.dps.rm.router.resource;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resource.ScrapeTargetHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the scrape target route.
 *
 * @author matthi-g
 */
public class ScrapeTargetRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ScrapeTargetHandler scrapeTargetHandler = new ScrapeTargetHandler(
            serviceProxyProvider.getFunctionDeploymentService());
        ResultHandler resultHandler = new ResultHandler(scrapeTargetHandler);

        router
            .operation("listScrapeTargets")
            .handler(rc -> resultHandler.handleFindAllRequest(rc, scrapeTargetHandler.getAllScrapeTargets()));
    }
}
