package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the vpc entity.
 *
 * @author matthi-g
 */
public class VPCHandler extends ValidationHandler {

    private final VPCChecker vpcChecker;

    /**
     * Create an instance from the vpcChecker.
     *
     * @param vpcChecker the vpc checker
     */
    public VPCHandler(VPCChecker vpcChecker) {
        super(vpcChecker);
        this.vpcChecker = vpcChecker;
    }

    @Override
    protected Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        long accountId = rc.user().principal().getLong("account_id");
        Account account = new Account();
        account.setAccountId(accountId);
        requestBody.put("created_by", JsonObject.mapFrom(account));
        return vpcChecker.submitCreate(accountId, requestBody);
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return vpcChecker.checkFindAll(accountId);
    }

    @Override
    public Completable deleteOne(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMapCompletable(id -> vpcChecker.submitDelete(accountId, id));
    }
}
