package at.uibk.dps.rm.router.account;

import at.uibk.dps.rm.rx.handler.PrivateEntityResultHandler;
import at.uibk.dps.rm.rx.handler.account.CredentialsHandler;
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
        CredentialsHandler credentialsHandler = new CredentialsHandler(serviceProxyProvider.getCredentialsService());
        PrivateEntityResultHandler resultHandler = new PrivateEntityResultHandler(credentialsHandler);

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
