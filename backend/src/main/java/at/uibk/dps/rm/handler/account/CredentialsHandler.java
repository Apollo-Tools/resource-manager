package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.ValidationHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
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
}
