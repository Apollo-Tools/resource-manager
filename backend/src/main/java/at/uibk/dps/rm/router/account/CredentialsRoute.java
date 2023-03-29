package at.uibk.dps.rm.router.account;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.account.AccountCredentialsChecker;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.account.CredentialsHandler;
import at.uibk.dps.rm.handler.resourceprovider.ResourceProviderChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class CredentialsRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        CredentialsChecker credentialsChecker = new CredentialsChecker(serviceProxyProvider.getCredentialsService());
        AccountCredentialsChecker accountCredentialsChecker = new AccountCredentialsChecker(serviceProxyProvider
            .getAccountCredentialsService());
        ResourceProviderChecker resourceProviderChecker = new ResourceProviderChecker(serviceProxyProvider
            .getResourceProviderService());
        CredentialsHandler credentialsHandler = new CredentialsHandler(credentialsChecker, accountCredentialsChecker,
            resourceProviderChecker);
        RequestHandler requestHandler = new RequestHandler(credentialsHandler);

        router
            .operation("addCredentials")
            .handler(requestHandler::postRequest);

        router
            .operation("listCredentials")
            .handler(requestHandler::getAllRequest);

        router
            .operation("deleteCredentials")
            .handler(requestHandler::deleteRequest);
    }
}
