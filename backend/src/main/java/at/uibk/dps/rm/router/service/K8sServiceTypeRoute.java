package at.uibk.dps.rm.router.service;

import at.uibk.dps.rm.handler.ResultHandler;
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
        K8sServiceTypeHandler serviceTypeHandler = new K8sServiceTypeHandler(serviceProxyProvider
            .getK8sServiceTypeService());
        ResultHandler resultHandler = new ResultHandler(serviceTypeHandler);

        router
            .operation("listK8sServiceTypes")
            .handler(resultHandler::handleFindAllRequest);
    }
}
