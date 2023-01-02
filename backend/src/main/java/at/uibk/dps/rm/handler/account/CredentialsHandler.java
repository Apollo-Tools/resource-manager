package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.resourceprovider.ResourceProviderChecker;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountCredentialsService;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.ResourceProviderService;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class CredentialsHandler extends ValidationHandler {

    private final CredentialsChecker credentialsChecker;

    private final AccountCredentialsChecker accountCredentialsChecker;

    private final ResourceProviderChecker resourceProviderChecker;

    public CredentialsHandler(CredentialsService credentialsService, AccountCredentialsService accountCredentialsService,
                              ResourceProviderService resourceProviderService) {
        super(new CredentialsChecker(credentialsService));
        this.credentialsChecker = (CredentialsChecker) super.entityChecker;
        this.accountCredentialsChecker = new AccountCredentialsChecker(accountCredentialsService);
        this.resourceProviderChecker = new ResourceProviderChecker(resourceProviderService);
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
