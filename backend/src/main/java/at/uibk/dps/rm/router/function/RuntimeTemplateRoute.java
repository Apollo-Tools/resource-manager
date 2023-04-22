package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.function.RuntimeChecker;
import at.uibk.dps.rm.handler.function.RuntimeTemplateHandler;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the runtime template route.
 *
 * @author matthi-g
 */
public class RuntimeTemplateRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        RuntimeChecker runtimeChecker = new RuntimeChecker(serviceProxyProvider.getRuntimeService());
        FileSystemChecker fileSystemChecker = new FileSystemChecker(serviceProxyProvider.getFilePathService());
        RuntimeTemplateHandler runtimeTemplateHandler =
            new RuntimeTemplateHandler(runtimeChecker, fileSystemChecker);
        ResultHandler resultHandler = new ResultHandler(runtimeTemplateHandler);

        router
            .operation("getRuntimeTemplate")
            .handler(resultHandler::handleFindOneRequest);
    }
}
