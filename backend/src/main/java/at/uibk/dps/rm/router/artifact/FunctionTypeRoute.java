package at.uibk.dps.rm.router.artifact;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.artifact.FunctionTypeChecker;
import at.uibk.dps.rm.handler.artifact.FunctionTypeHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the resource route.
 *
 * @author matthi-g
 */
public class FunctionTypeRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        FunctionTypeChecker functionTypeChecker = new FunctionTypeChecker(serviceProxyProvider.getFunctionTypeService());
        FunctionTypeHandler functionTypeHandler = new FunctionTypeHandler(functionTypeChecker);
        ResultHandler resultHandler = new ResultHandler(functionTypeHandler);

        router
            .operation("createFunctionType")
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("listFunctionTypes")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("getFunctionType")
            .handler(resultHandler::handleFindOneRequest);

        router
            .operation("deleteFunctionType")
            .handler(resultHandler::handleDeleteRequest);
    }
}
