package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resourceprovider.*;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class VPCRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        VPCChecker vpcChecker = new VPCChecker(serviceProxyProvider.getVpcService());
        RegionChecker regionChecker = new RegionChecker(serviceProxyProvider.getRegionService());
        VPCHandler vpcHandler = new VPCHandler(vpcChecker, regionChecker);
        ResultHandler resultHandler = new ResultHandler(vpcHandler);

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
