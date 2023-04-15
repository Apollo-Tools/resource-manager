package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.handler.ValidationHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class VPCHandler extends ValidationHandler {

    private final VPCChecker vpcChecker;

    private final RegionChecker regionChecker;

    public VPCHandler(VPCChecker vpcChecker, RegionChecker regionChecker) {
        super(vpcChecker);
        this.vpcChecker = vpcChecker;
        this.regionChecker = regionChecker;
    }

    @Override
    protected Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        long regionId = requestBody.getJsonObject("region").getLong("region_id");
        long accountId = rc.user().principal().getLong("account_id");
        return vpcChecker.checkForDuplicateEntity(requestBody, accountId)
            .andThen(regionChecker.checkExistsOne(regionId))
            // see https://stackoverflow.com/a/50670502/13164629 for further information
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMap(result -> {
                Account account = new Account();
                account.setAccountId(accountId);
                requestBody.put("created_by", JsonObject.mapFrom(account));
                return entityChecker.submitCreate(requestBody);
            });
    }



}
