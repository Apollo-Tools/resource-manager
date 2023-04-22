package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.function.FunctionChecker;
import at.uibk.dps.rm.handler.function.FunctionResourceSLOHandler;
import at.uibk.dps.rm.handler.function.SLOInputHandler;
import at.uibk.dps.rm.handler.metric.MetricChecker;
import at.uibk.dps.rm.handler.metric.MetricValueChecker;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the function resource slo route.
 *
 * @author matthi-g
 */
public class FunctionResourceSLORoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        FunctionChecker functionChecker = new FunctionChecker(serviceProxyProvider.getFunctionService());
        ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        MetricChecker metricChecker = new MetricChecker(serviceProxyProvider.getMetricService());
        MetricValueChecker metricValueChecker = new MetricValueChecker(serviceProxyProvider
            .getMetricValueService());
        FunctionResourceSLOHandler sloHandler = new FunctionResourceSLOHandler(functionChecker, resourceChecker,
            metricChecker, metricValueChecker);
        ResultHandler resultHandler = new ResultHandler(null);

        router
            .operation("listFunctionResourcesBySLOs")
            .handler(SLOInputHandler::validateGetResourcesBySLOsRequest)
            .handler(rc -> resultHandler.handleFindAllRequest(rc, sloHandler.getFunctionResourceBySLOs(rc)));
    }
}
