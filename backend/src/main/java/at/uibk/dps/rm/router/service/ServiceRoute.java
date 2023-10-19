package at.uibk.dps.rm.router.service;

import at.uibk.dps.rm.handler.PrivateEntityResultHandler;
import at.uibk.dps.rm.handler.service.ServiceHandler;
import at.uibk.dps.rm.handler.service.ServiceInputHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the service route.
 *
 * @author matthi-g
 */
public class ServiceRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ServiceHandler serviceHandler = new ServiceHandler(serviceProxyProvider.getServiceService());
        PrivateEntityResultHandler resultHandler = new PrivateEntityResultHandler(serviceHandler);

        router
            .operation("createService")
            .handler(ServiceInputHandler::validateAddServiceRequest)
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("updateService")
            .handler(ServiceInputHandler::validateUpdateServiceRequest)
            .handler(resultHandler::handleUpdateRequest);

        router
            .operation("listMyServices")
            .handler(rc -> resultHandler.handleFindAllRequest(rc, serviceHandler.getAllFromAccount(rc)));

        router
            .operation("listAllServices")
            .handler(rc -> resultHandler.handleFindAllRequest(rc, serviceHandler.getAll(rc)));

        router
            .operation("getService")
            .handler(resultHandler::handleFindOneRequest);

        router
            .operation("deleteService")
            .handler(resultHandler::handleDeleteRequest);
    }
}
