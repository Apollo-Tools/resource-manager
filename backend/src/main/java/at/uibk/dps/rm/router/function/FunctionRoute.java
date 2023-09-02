package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.rx.handler.PrivateEntityResultHandler;
import at.uibk.dps.rm.rx.handler.function.FunctionHandler;
import at.uibk.dps.rm.rx.handler.function.FunctionInputHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the function route.
 *
 * @author matthi-g
 */
public class FunctionRoute implements Route {

    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        FunctionHandler functionHandler = new FunctionHandler(serviceProxyProvider.getFunctionService());
        PrivateEntityResultHandler resultHandler = new PrivateEntityResultHandler(functionHandler);

        router
            .operation("createFunction")
            .handler(FunctionInputHandler::validateAddFunctionRequest)
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("createFunctionFile")
            .handler(FunctionInputHandler::validateAddFunctionRequest)
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("listMyFunctions")
            .handler(rc -> resultHandler.handleFindAllRequest(rc, functionHandler.getAllFromAccount(rc)));

        router
            .operation("listAllFunctions")
            .handler(rc -> resultHandler.handleFindAllRequest(rc, functionHandler.getAll(rc)));

        router
            .operation("getFunction")
            .handler(resultHandler::handleFindOneRequest);

        router
            .operation("updateFunction")
            .handler(FunctionInputHandler::validateUpdateFunctionRequest)
            .handler(resultHandler::handleUpdateRequest);

        router
            .operation("updateFunctionFile")
            .handler(FunctionInputHandler::validateUpdateFunctionRequest)
            .handler(resultHandler::handleUpdateRequest);

        router
            .operation("deleteFunction")
            .handler(resultHandler::handleDeleteRequest);
    }
}
