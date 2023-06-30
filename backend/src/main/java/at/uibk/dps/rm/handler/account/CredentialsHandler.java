package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the credentials entity.
 *
 * @author matthi-g
 */
public class CredentialsHandler extends ValidationHandler {

    private final CredentialsChecker credentialsChecker;

    /**
     * Create an instance from the credentialsChecker.
     *
     * @param credentialsChecker the credentials checker
     */
    public CredentialsHandler(CredentialsChecker credentialsChecker) {
        super(credentialsChecker);
        this.credentialsChecker = credentialsChecker;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return credentialsChecker.checkFindAll(accountId);
    }

    @Override
    protected Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        long accountId = rc.user().principal().getLong("account_id");
        return credentialsChecker.submitCreate(accountId, requestBody);
    }

    @Override
    public Completable deleteOne(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMapCompletable(id -> credentialsChecker.submitDelete(accountId, id));
    }
}
