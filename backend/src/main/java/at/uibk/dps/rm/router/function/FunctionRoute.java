package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.function.FunctionChecker;
import at.uibk.dps.rm.handler.function.FunctionHandler;
import at.uibk.dps.rm.handler.function.FunctionInputHandler;
import at.uibk.dps.rm.handler.function.RuntimeChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class FunctionRoute {

    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        FunctionChecker functionChecker = new FunctionChecker(serviceProxyProvider.getFunctionService());
        RuntimeChecker runtimeChecker = new RuntimeChecker(serviceProxyProvider.getRuntimeService());
        FunctionHandler functionHandler = new FunctionHandler(functionChecker, runtimeChecker);
        ResultHandler resultHandler = new ResultHandler(functionHandler);

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
