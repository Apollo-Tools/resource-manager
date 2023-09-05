package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the credentials entity.
 *
 * @author matthi-g
 */
public class CredentialsHandler extends ValidationHandler {

    private final CredentialsService credentialsService;

    /**
     * Create an instance from the credentialsService.
     *
     * @param credentialsService the service
     */
    public CredentialsHandler(CredentialsService credentialsService) {
        super(credentialsService);
        this.credentialsService = credentialsService;
    }



    @Override
    protected Single<JsonArray> getAllFromAccount(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return credentialsService.findAllByAccountIdAndIncludeExcludeSecrets(accountId, false);
    }
}
