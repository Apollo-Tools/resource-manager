package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.function.FunctionChecker;
import at.uibk.dps.rm.handler.function.FunctionResourceChecker;
import at.uibk.dps.rm.handler.function.FunctionResourceHandler;
import at.uibk.dps.rm.handler.function.FunctionResourceInputHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the function resource route.
 *
 * @author matthi-g
 */
public class FunctionResourceRoute implements Route {

    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        FunctionResourceChecker functionResourceChecker = new FunctionResourceChecker(serviceProxyProvider
            .getFunctionResourceService());
        FunctionChecker functionChecker = new FunctionChecker(serviceProxyProvider.getFunctionService());
        ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        FunctionResourceHandler functionResourceHandler = new FunctionResourceHandler(functionResourceChecker,
            functionChecker, resourceChecker);
        ResultHandler resultHandler = new ResultHandler(functionResourceHandler);

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
