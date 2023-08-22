package at.uibk.dps.rm.router.account;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.account.AccountChecker;
import at.uibk.dps.rm.handler.account.AccountHandler;
import at.uibk.dps.rm.handler.account.AccountInputHandler;
import at.uibk.dps.rm.router.AuthenticationRoute;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.configuration.JWTAuthProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the account route.
 *
 * @author matthi-g
 */
public class AccountRoute implements AuthenticationRoute {

    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider,
        JWTAuthProvider jwtAuthProvider) {
        AccountChecker accountChecker = new AccountChecker(serviceProxyProvider.getAccountService());
        AccountHandler accountHandler = new AccountHandler(accountChecker, jwtAuthProvider.getJwtAuth());
        ResultHandler resultHandler = new ResultHandler(accountHandler);

        router
            .operation("getMyAccount")
            .handler(rc -> resultHandler.handleFindOneRequest(rc, accountHandler.getOne(rc, true)));

        router
            .operation("getAccount")
            .handler(JWTAuthProvider.getAdminAuthorizationHandler())
            .handler(rc -> resultHandler.handleFindOneRequest(rc, accountHandler.getOne(rc, false)));

        router
            .operation("listAccounts")
            .handler(JWTAuthProvider.getAdminAuthorizationHandler())
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("lockAccount")
            .handler(JWTAuthProvider.getAdminAuthorizationHandler())
            .handler(rc -> resultHandler.handleUpdateRequest(rc, accountHandler.lockAccount(rc)));

        router
            .operation("unlockAccount")
            .handler(JWTAuthProvider.getAdminAuthorizationHandler())
            .handler(rc -> resultHandler.handleUpdateRequest(rc, accountHandler.unlockAccount(rc)));

        router
            .operation("signUp")
            .handler(JWTAuthProvider.getAdminAuthorizationHandler())
            .handler(AccountInputHandler::validateSignupRequest)
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("login")
            .handler(rc -> resultHandler.handleFindOneRequest(rc, accountHandler.login(rc)));

        router
            .operation("changePassword")
            .handler(AccountInputHandler::validateChangePasswordRequest)
            .handler(resultHandler::handleUpdateRequest);
    }
}
