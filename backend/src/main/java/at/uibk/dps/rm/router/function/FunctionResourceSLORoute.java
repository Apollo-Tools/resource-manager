package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.function.FunctionResourceSLOHandler;
import at.uibk.dps.rm.handler.function.SLOInputHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class FunctionResourceSLORoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        FunctionResourceSLOHandler sloHandler = new FunctionResourceSLOHandler(serviceProxyProvider);

        router
            .operation("listFunctionResourcesBySLOs")
            .handler(SLOInputHandler::validateGetResourcesBySLOsRequest)
            .handler(rc -> ResultHandler.handleGetAllRequest(rc, sloHandler.getFunctionResourceBySLOs(rc)));
    }
}
