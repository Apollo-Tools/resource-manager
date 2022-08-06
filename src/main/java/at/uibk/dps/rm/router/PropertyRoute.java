package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.property.PropertyHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class PropertyRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        PropertyHandler propertyHandler = new PropertyHandler(serviceProxyProvider.getPropertyService());
        RequestHandler requestHandler = new RequestHandler(propertyHandler);

        router
            .operation("createProperty")
            .handler(requestHandler::postRequest);

        router
            .operation("listProperties")
            .handler(requestHandler::getAllRequest);

        router
            .operation("getProperty")
            .handler(requestHandler::getRequest);

        router
            .operation("updateProperty")
            .handler(requestHandler::patchRequest);

        router
            .operation("deleteProperty")
            .handler(requestHandler::deleteRequest);
    }
}
