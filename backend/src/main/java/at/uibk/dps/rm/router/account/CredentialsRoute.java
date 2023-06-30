package at.uibk.dps.rm.router.account;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.account.CredentialsHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the credentials route.
 *
 * @author matthi-g
 */
public class CredentialsRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        CredentialsChecker credentialsChecker = new CredentialsChecker(serviceProxyProvider
            .getCredentialsService());
        CredentialsHandler credentialsHandler = new CredentialsHandler(credentialsChecker);
        ResultHandler resultHandler = new ResultHandler(credentialsHandler);

        router
            .operation("addCredentials")
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("listCredentials")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("deleteCredentials")
            .handler(resultHandler::handleDeleteRequest);
    }
}
