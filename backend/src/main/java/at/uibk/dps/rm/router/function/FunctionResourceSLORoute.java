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
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        FunctionChecker functionChecker = new FunctionChecker(serviceProxyProvider.getFunctionService());
        ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        MetricChecker metricChecker = new MetricChecker(serviceProxyProvider.getMetricService());
        MetricValueChecker metricValueChecker = new MetricValueChecker(serviceProxyProvider.getMetricValueService());
        FunctionResourceSLOHandler sloHandler = new FunctionResourceSLOHandler(functionChecker, resourceChecker,
            metricChecker, metricValueChecker);

        router
            .operation("listFunctionResourcesBySLOs")
            .handler(SLOInputHandler::validateGetResourcesBySLOsRequest)
            .handler(rc -> ResultHandler.handleGetAllRequest(rc, sloHandler.getFunctionResourceBySLOs(rc)));
    }
}
