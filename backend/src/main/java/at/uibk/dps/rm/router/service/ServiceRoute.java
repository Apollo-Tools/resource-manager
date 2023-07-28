package at.uibk.dps.rm.router.service;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.service.ServiceChecker;
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
        ServiceChecker serviceChecker = new ServiceChecker(serviceProxyProvider.getServiceService());
        ServiceHandler serviceHandler = new ServiceHandler(serviceChecker);
        ResultHandler resultHandler = new ResultHandler(serviceHandler);

        router
            .operation("createService")
            .handler(ServiceInputHandler::validateAddServiceRequest)
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("updateService")
            .handler(resultHandler::handleUpdateRequest);

        router
            .operation("listServices")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("getService")
            .handler(resultHandler::handleFindOneRequest);

        router
            .operation("deleteService")
            .handler(resultHandler::handleDeleteRequest);
    }
}
