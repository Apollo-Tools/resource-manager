package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.handler.PrivateEntityResultHandler;
import at.uibk.dps.rm.handler.resourceprovider.*;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the vpc route.
 *
 * @author matthi-g
 */
public class VPCRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        VPCChecker vpcChecker = new VPCChecker(serviceProxyProvider.getVpcService());
        VPCHandler vpcHandler = new VPCHandler(vpcChecker);
        PrivateEntityResultHandler resultHandler = new PrivateEntityResultHandler(vpcHandler);

        router
            .operation("createVPC")
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("listVPCs")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("getVPC")
            .handler(resultHandler::handleFindOneRequest);

        router
            .operation("deleteVPC")
            .handler(resultHandler::handleDeleteRequest);
    }
}
