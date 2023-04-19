package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.function.RuntimeChecker;
import at.uibk.dps.rm.handler.function.RuntimeTemplateHandler;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class RuntimeTemplateRoute {
    public static void init(final RouterBuilder router, final ServiceProxyProvider serviceProxyProvider) {
        final RuntimeChecker runtimeChecker = new RuntimeChecker(serviceProxyProvider.getRuntimeService());
        final FileSystemChecker fileSystemChecker = new FileSystemChecker(serviceProxyProvider.getFilePathService());
        final RuntimeTemplateHandler runtimeTemplateHandler =
            new RuntimeTemplateHandler(runtimeChecker, fileSystemChecker);
        final ResultHandler resultHandler = new ResultHandler(runtimeTemplateHandler);

        router
            .operation("getRuntimeTemplate")
            .handler(resultHandler::handleFindOneRequest);
    }
}
