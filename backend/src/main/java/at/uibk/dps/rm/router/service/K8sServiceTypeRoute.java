package at.uibk.dps.rm.router.service;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.service.K8sServiceTypeChecker;
import at.uibk.dps.rm.handler.service.K8sServiceTypeHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the service type route.
 *
 * @author matthi-g
 */
public class K8sServiceTypeRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        K8sServiceTypeChecker serviceTypeChecker = new K8sServiceTypeChecker(serviceProxyProvider.getK8sServiceTypeService());
        K8sServiceTypeHandler serviceTypeHandler = new K8sServiceTypeHandler(serviceTypeChecker);
        ResultHandler resultHandler = new ResultHandler(serviceTypeHandler);

        router
            .operation("listK8sServiceTypes")
            .handler(resultHandler::handleFindAllRequest);
    }
}
