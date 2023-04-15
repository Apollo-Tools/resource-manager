package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.resourceprovider.*;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class VPCRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        VPCChecker vpcChecker = new VPCChecker(serviceProxyProvider.getVpcService());
        RegionChecker regionChecker = new RegionChecker(serviceProxyProvider.getRegionService());
        VPCHandler vpcHandler = new VPCHandler(vpcChecker, regionChecker);
        RequestHandler requestHandler = new RequestHandler(vpcHandler);

        router
            .operation("createVPC")
            .handler(requestHandler::postRequest);

        router
            .operation("listVPCs")
            .handler(requestHandler::getAllRequest);

        router
            .operation("getVPC")
            .handler(requestHandler::getRequest);

        router
            .operation("deleteVPC")
            .handler(requestHandler::deleteRequest);
    }
}
