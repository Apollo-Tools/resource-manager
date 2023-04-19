package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.function.FunctionChecker;
import at.uibk.dps.rm.handler.function.FunctionHandler;
import at.uibk.dps.rm.handler.function.FunctionInputHandler;
import at.uibk.dps.rm.handler.function.RuntimeChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class FunctionRoute {

    public static void init(final RouterBuilder router, final ServiceProxyProvider serviceProxyProvider) {
        final FunctionChecker functionChecker = new FunctionChecker(serviceProxyProvider.getFunctionService());
        final RuntimeChecker runtimeChecker = new RuntimeChecker(serviceProxyProvider.getRuntimeService());
        final FunctionHandler functionHandler = new FunctionHandler(functionChecker, runtimeChecker);
        final ResultHandler resultHandler = new ResultHandler(functionHandler);

        router
            .operation("createFunction")
            .handler(FunctionInputHandler::validateAddFunctionRequest)
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("listFunctions")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("getFunction")
            .handler(resultHandler::handleFindOneRequest);

        router
            .operation("updateFunction")
            .handler(resultHandler::handleUpdateRequest);

        router
            .operation("deleteFunction")
            .handler(resultHandler::handleDeleteRequest);
    }
}
