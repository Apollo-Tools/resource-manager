package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.VPCService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class VPCChecker extends EntityChecker {

    private final VPCService vpcService;

    public VPCChecker(VPCService vpcService) {
        super(vpcService);
        this.vpcService = vpcService;
    }

    public Single<JsonObject> checkFindOneByRegionIdAndAccountId(long regionId, long accountId) {
        Single<JsonObject> findOneByRegionIdAndAccountId = vpcService.findOneByRegionIdAndAccountId(regionId, accountId);
        return ErrorHandler.handleFindOne(findOneByRegionIdAndAccountId);
    }
}
