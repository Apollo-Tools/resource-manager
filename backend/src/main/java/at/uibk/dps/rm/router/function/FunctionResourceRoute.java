package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.function.FunctionChecker;
import at.uibk.dps.rm.handler.function.FunctionResourceChecker;
import at.uibk.dps.rm.handler.function.FunctionResourceHandler;
import at.uibk.dps.rm.handler.function.FunctionResourceInputHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class FunctionResourceRoute {

    public static void init(final RouterBuilder router, final ServiceProxyProvider serviceProxyProvider) {
        final FunctionResourceChecker functionResourceChecker = new FunctionResourceChecker(serviceProxyProvider
            .getFunctionResourceService());
        final FunctionChecker functionChecker = new FunctionChecker(serviceProxyProvider.getFunctionService());
        final ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        final FunctionResourceHandler functionResourceHandler = new FunctionResourceHandler(functionResourceChecker,
            functionChecker, resourceChecker);
        final ResultHandler resultHandler = new ResultHandler(functionResourceHandler);

        router
            .operation("addFunctionResources")
            .handler(FunctionResourceInputHandler::validateAddFunctionResourceRequest)
            .handler(resultHandler::handleSaveAllRequest);

        router
            .operation("listFunctionResources")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("deleteFunctionResource")
            .handler(resultHandler::handleDeleteRequest);
    }
}
