package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.account.CredentialsHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class CredentialsRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        CredentialsHandler credentialsHandler = new CredentialsHandler(serviceProxyProvider.getCredentialsService(),
            serviceProxyProvider.getAccountCredentialsService(), serviceProxyProvider.getCloudProviderService());
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
