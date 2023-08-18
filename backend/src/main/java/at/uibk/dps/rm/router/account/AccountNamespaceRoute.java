package at.uibk.dps.rm.router.account;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.account.AccountNamespaceChecker;
import at.uibk.dps.rm.handler.account.AccountNamespaceHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the account namespace route.
 *
 * @author matthi-g
 */
public class AccountNamespaceRoute implements Route {

    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        AccountNamespaceChecker checker =
            new AccountNamespaceChecker(serviceProxyProvider.getAccountNamespaceService());
        AccountNamespaceHandler accountHandler = new AccountNamespaceHandler(checker);
        ResultHandler resultHandler = new ResultHandler(accountHandler);

        router
            .operation("addNamespaceToAccount")
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("deleteNamespaceFromAccount")
            .handler(resultHandler::handleDeleteRequest);
    }
}
