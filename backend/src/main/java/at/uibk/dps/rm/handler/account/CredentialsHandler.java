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

    private final AccountCredentialsChecker accountCredentialsChecker;

    /**
     * Create an instance from the credentialsChecker and accountCredentialsChecker.
     *
     * @param credentialsChecker the credentials checker
     * @param accountCredentialsChecker the account credentials checker
     */
    public CredentialsHandler(CredentialsChecker credentialsChecker,
            AccountCredentialsChecker accountCredentialsChecker) {
        super(credentialsChecker);
        this.credentialsChecker = credentialsChecker;
        this.accountCredentialsChecker = accountCredentialsChecker;
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
            .flatMap(id -> entityChecker.checkFindOne(id)
                .flatMap(result -> accountCredentialsChecker.checkFindOneByCredentialsAndAccount(id, accountId)))
            .flatMap(result -> accountCredentialsChecker
                .submitDelete(result.getLong("account_credentials_id"))
                .andThen(Single.just(result)))
            .flatMapCompletable(result -> entityChecker.submitDelete(result.getJsonObject("credentials")
                .getLong("credentials_id")));
    }
}
