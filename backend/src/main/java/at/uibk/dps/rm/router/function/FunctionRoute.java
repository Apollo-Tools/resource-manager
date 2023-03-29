package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.function.FunctionChecker;
import at.uibk.dps.rm.handler.function.FunctionHandler;
import at.uibk.dps.rm.handler.function.RuntimeChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class FunctionRoute {

    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        FunctionChecker functionChecker = new FunctionChecker(serviceProxyProvider.getFunctionService());
        RuntimeChecker runtimeChecker = new RuntimeChecker(serviceProxyProvider.getRuntimeService());
        FunctionHandler functionHandler = new FunctionHandler(functionChecker, runtimeChecker);
        RequestHandler functionRequestHandler = new RequestHandler(functionHandler);

        router
            .operation("createFunction")
            .handler(functionRequestHandler::postRequest);

        router
            .operation("listFunctions")
            .handler(functionRequestHandler::getAllRequest);

        router
            .operation("getFunction")
            .handler(functionRequestHandler::getRequest);

        router
            .operation("updateFunction")
            .handler(functionRequestHandler::patchRequest);

        router
            .operation("deleteFunction")
            .handler(functionRequestHandler::deleteRequest);
    }
}
