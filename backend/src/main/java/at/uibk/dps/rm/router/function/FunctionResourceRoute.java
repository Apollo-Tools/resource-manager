package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.function.FunctionResourceHandler;
import at.uibk.dps.rm.handler.function.FunctionResourceInputHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class FunctionResourceRoute {

    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        FunctionResourceHandler functionResourceHandler = new FunctionResourceHandler(
            serviceProxyProvider.getFunctionResourceService(), serviceProxyProvider.getFunctionService(),
            serviceProxyProvider.getResourceService());
        RequestHandler functionResourceRequestHandler = new RequestHandler(functionResourceHandler);

        router
            .operation("addFunctionResources")
            .handler(FunctionResourceInputHandler::validateAddFunctionResourceRequest)
            .handler(functionResourceRequestHandler::postAllRequest);

        router
            .operation("listFunctionResources")
            .handler(functionResourceRequestHandler::getAllRequest);

        router
            .operation("deleteFunctionResource")
            .handler(functionResourceRequestHandler::deleteRequest);
    }
}
