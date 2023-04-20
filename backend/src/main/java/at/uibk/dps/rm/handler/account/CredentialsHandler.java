package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.resourceprovider.ResourceProviderChecker;
import at.uibk.dps.rm.util.HttpHelper;
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

    private final ResourceProviderChecker resourceProviderChecker;

    /**
     * Create an instance from the credentialsChecker, accountCredentialsChecker and
     * resourceProviderChecker
     *
     * @param credentialsChecker the credentials checker
     * @param accountCredentialsChecker the account credentials checker
     * @param resourceProviderChecker the resource provider checker
     */
    public CredentialsHandler(CredentialsChecker credentialsChecker, AccountCredentialsChecker accountCredentialsChecker,
                              ResourceProviderChecker resourceProviderChecker) {
        super(credentialsChecker);
        this.credentialsChecker = credentialsChecker;
        this.accountCredentialsChecker = accountCredentialsChecker;
        this.resourceProviderChecker = resourceProviderChecker;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return credentialsChecker.checkFindAll(accountId);
    }

    @Override
    protected Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        long providerId = requestBody.getJsonObject("resource_provider").getLong("provider_id");
        long accountId = rc.user().principal().getLong("account_id");
        return accountCredentialsChecker.checkForDuplicateEntity(requestBody, accountId)
            .andThen(resourceProviderChecker.checkExistsOne(providerId))
            // see https://stackoverflow.com/a/50670502/13164629 for further information
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMap(result -> entityChecker.submitCreate(requestBody))
            .map(result -> {
                JsonObject accountCredentials = new JsonObject();
                accountCredentials.put("account", new JsonObject("{\"account_id\": " + accountId + "}"));
                accountCredentials.put("credentials", result);
                return accountCredentials;
            })
            .flatMap(accountCredentialsChecker::submitCreate);
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
