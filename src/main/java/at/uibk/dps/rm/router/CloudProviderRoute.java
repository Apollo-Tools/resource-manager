package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.cloudprovider.CloudProviderHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class CloudProviderRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        CloudProviderHandler cloudProviderHandler = new CloudProviderHandler(
            serviceProxyProvider.getCloudProviderService());
        RequestHandler requestHandler = new RequestHandler(cloudProviderHandler);

        router
            .operation("createCloudProvider")
            .handler(requestHandler::postRequest);

        router
            .operation("listCloudProviders")
            .handler(requestHandler::getAllRequest);

        router
            .operation("getCloudProvider")
            .handler(requestHandler::getRequest);

        router
            .operation("deleteCloudProvider")
            .handler(requestHandler::deleteRequest);
    }
}
