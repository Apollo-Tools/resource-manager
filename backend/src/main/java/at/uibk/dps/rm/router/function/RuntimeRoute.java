package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.function.RuntimeChecker;
import at.uibk.dps.rm.handler.function.RuntimeHandler;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the runtime route.
 *
 * @author matthi-g
 */
public class RuntimeRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        RuntimeChecker runtimeChecker = new RuntimeChecker(serviceProxyProvider.getRuntimeService());
        FileSystemChecker fileSystemChecker = new FileSystemChecker(serviceProxyProvider.getFilePathService());
        RuntimeHandler runtimeHandler = new RuntimeHandler(runtimeChecker, fileSystemChecker);
        ResultHandler resultHandler = new ResultHandler(runtimeHandler);

        router
            .operation("listRuntimes")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("getRuntime")
            .handler(resultHandler::handleFindOneRequest);
    }
}
