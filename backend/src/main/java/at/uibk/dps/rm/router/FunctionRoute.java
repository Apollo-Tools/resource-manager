package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.function.FunctionHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class FunctionRoute {

    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        FunctionHandler functionHandler = new FunctionHandler(serviceProxyProvider.getFunctionService(),
            serviceProxyProvider.getRuntimeService());
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
