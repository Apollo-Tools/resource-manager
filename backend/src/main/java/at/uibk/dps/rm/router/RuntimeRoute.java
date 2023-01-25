package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.function.RuntimeHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class RuntimeRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        RuntimeHandler runtimeHandler = new RuntimeHandler(serviceProxyProvider.getRuntimeService(),
            serviceProxyProvider.getFilePathService());
        RequestHandler runtimeRequestHandler = new RequestHandler(runtimeHandler);

        router
            .operation("createRuntime")
            .handler(runtimeRequestHandler::postRequest);

        router
            .operation("listRuntimes")
            .handler(runtimeRequestHandler::getAllRequest);

        router
            .operation("getRuntime")
            .handler(runtimeRequestHandler::getRequest);

        router
            .operation("updateRuntime")
            .handler(runtimeRequestHandler::patchRequest);

        router
            .operation("deleteRuntime")
            .handler(runtimeRequestHandler::deleteRequest);
    }
}
