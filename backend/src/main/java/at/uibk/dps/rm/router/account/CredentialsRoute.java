package at.uibk.dps.rm.router.account;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.account.AccountCredentialsChecker;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.account.CredentialsHandler;
import at.uibk.dps.rm.handler.resourceprovider.ResourceProviderChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class CredentialsRoute {
    public static void init(final RouterBuilder router, final ServiceProxyProvider serviceProxyProvider) {
        final CredentialsChecker credentialsChecker = new CredentialsChecker(serviceProxyProvider
            .getCredentialsService());
        final AccountCredentialsChecker accountCredentialsChecker = new AccountCredentialsChecker(serviceProxyProvider
            .getAccountCredentialsService());
        final ResourceProviderChecker resourceProviderChecker = new ResourceProviderChecker(serviceProxyProvider
            .getResourceProviderService());
        final CredentialsHandler credentialsHandler = new CredentialsHandler(credentialsChecker,
            accountCredentialsChecker, resourceProviderChecker);
        final ResultHandler resultHandler = new ResultHandler(credentialsHandler);

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
