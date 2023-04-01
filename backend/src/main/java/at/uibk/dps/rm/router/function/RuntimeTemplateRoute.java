package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.function.RuntimeChecker;
import at.uibk.dps.rm.handler.function.RuntimeTemplateHandler;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class RuntimeTemplateRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        RuntimeChecker runtimeChecker = new RuntimeChecker(serviceProxyProvider.getRuntimeService());
        FileSystemChecker fileSystemChecker = new FileSystemChecker(serviceProxyProvider.getFilePathService());
        RuntimeTemplateHandler runtimeTemplateHandler = new RuntimeTemplateHandler(runtimeChecker, fileSystemChecker);
        RequestHandler runtimeTemplateRequestHandler = new RequestHandler(runtimeTemplateHandler);

        router
            .operation("getRuntimeTemplate")
            .handler(runtimeTemplateRequestHandler::getRequest);
    }
}