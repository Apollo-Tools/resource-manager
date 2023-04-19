package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.function.FunctionChecker;
import at.uibk.dps.rm.handler.function.FunctionResourceSLOHandler;
import at.uibk.dps.rm.handler.function.SLOInputHandler;
import at.uibk.dps.rm.handler.metric.MetricChecker;
import at.uibk.dps.rm.handler.metric.MetricValueChecker;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class FunctionResourceSLORoute {
    public static void init(final RouterBuilder router, final ServiceProxyProvider serviceProxyProvider) {
        final FunctionChecker functionChecker = new FunctionChecker(serviceProxyProvider.getFunctionService());
        final ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        final MetricChecker metricChecker = new MetricChecker(serviceProxyProvider.getMetricService());
        final MetricValueChecker metricValueChecker = new MetricValueChecker(serviceProxyProvider
            .getMetricValueService());
        final FunctionResourceSLOHandler sloHandler = new FunctionResourceSLOHandler(functionChecker, resourceChecker,
            metricChecker, metricValueChecker);
        final ResultHandler resultHandler = new ResultHandler(null);

        router
            .operation("listFunctionResourcesBySLOs")
            .handler(SLOInputHandler::validateGetResourcesBySLOsRequest)
            .handler(rc -> resultHandler.handleFindAllRequest(rc, sloHandler.getFunctionResourceBySLOs(rc)));
    }
}
