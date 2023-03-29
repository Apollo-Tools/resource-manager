package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.function.FunctionChecker;
import at.uibk.dps.rm.handler.function.FunctionResourceChecker;
import at.uibk.dps.rm.handler.function.FunctionResourceHandler;
import at.uibk.dps.rm.handler.function.FunctionResourceInputHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class FunctionResourceRoute {

    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        FunctionResourceChecker functionResourceChecker = new FunctionResourceChecker(serviceProxyProvider
            .getFunctionResourceService());
        FunctionChecker functionChecker = new FunctionChecker(serviceProxyProvider.getFunctionService());
        ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        FunctionResourceHandler functionResourceHandler = new FunctionResourceHandler(functionResourceChecker,
            functionChecker, resourceChecker);
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
