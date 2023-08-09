package at.uibk.dps.rm.router.artifact;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.artifact.ServiceTypeChecker;
import at.uibk.dps.rm.handler.artifact.ServiceTypeHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the resource route.
 *
 * @author matthi-g
 */
public class ServiceTypeRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ServiceTypeChecker serviceTypeChecker = new ServiceTypeChecker(serviceProxyProvider.getServiceTypeService());
        ServiceTypeHandler serviceTypeHandler = new ServiceTypeHandler(serviceTypeChecker);
        ResultHandler resultHandler = new ResultHandler(serviceTypeHandler);

        router
            .operation("createServiceType")
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("listServiceTypes")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("getServiceType")
            .handler(resultHandler::handleFindOneRequest);

        router
            .operation("deleteServiceType")
            .handler(resultHandler::handleDeleteRequest);
    }
}
