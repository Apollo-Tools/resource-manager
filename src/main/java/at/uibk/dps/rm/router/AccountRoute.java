package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.account.AccountHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.database.account.JWTAuthProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class AccountRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider, JWTAuthProvider jwtAuthProvider) {
        AccountHandler accountHandler = new AccountHandler(serviceProxyProvider.getAccountService(),
            jwtAuthProvider.getJwtAuth());
        RequestHandler requestHandler = new RequestHandler(accountHandler);


        router
            .operation("getAccount")
            .handler(requestHandler::getRequest);

        router
            .operation("signUp")
            .handler(requestHandler::postRequest);

        router
            .operation("login")
            .handler(rc -> ResultHandler.handleGetOneRequest(rc, accountHandler.login(rc)));
    }
}
