package at.uibk.dps.rm.router.service;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.service.ServiceTypeChecker;
import at.uibk.dps.rm.handler.service.ServiceTypeHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ServiceTypeRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ServiceTypeChecker serviceTypeChecker = new ServiceTypeChecker(serviceProxyProvider.getServiceTypeService());
        ServiceTypeHandler serviceTypeHandler = new ServiceTypeHandler(serviceTypeChecker);
        ResultHandler resultHandler = new ResultHandler(serviceTypeHandler);

        router
            .operation("listServiceTypes")
            .handler(resultHandler::handleFindAllRequest);
    }
}
