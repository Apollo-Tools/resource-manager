package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resourceprovider.*;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class VPCRoute {
    public static void init(final RouterBuilder router, final ServiceProxyProvider serviceProxyProvider) {
        final VPCChecker vpcChecker = new VPCChecker(serviceProxyProvider.getVpcService());
        final RegionChecker regionChecker = new RegionChecker(serviceProxyProvider.getRegionService());
        final VPCHandler vpcHandler = new VPCHandler(vpcChecker, regionChecker);
        final ResultHandler resultHandler = new ResultHandler(vpcHandler);

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
