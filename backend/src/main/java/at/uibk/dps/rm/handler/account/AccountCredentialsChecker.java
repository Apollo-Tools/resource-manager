package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountCredentialsService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class AccountCredentialsChecker extends EntityChecker {

    private final AccountCredentialsService accountCredentialsService;

    public AccountCredentialsChecker(AccountCredentialsService accountCredentialsService) {
        super(accountCredentialsService);
        this.accountCredentialsService = accountCredentialsService;
    }

    public Completable checkForDuplicateEntity(JsonObject credentials, long accountId) {
        Single<Boolean> existsOneByAccountAndProvider = accountCredentialsService
            .existsOneByAccountAndProvider(accountId, credentials.getJsonObject("resource_provider")
                .getLong("provider_id"));
        return ErrorHandler.handleDuplicates(existsOneByAccountAndProvider).ignoreElement();
    }

    public Single<JsonObject> checkFindOneByCredentialsAndAccount(long credentialsId, long accountId) {
        Single<JsonObject> findOneByCredentials = accountCredentialsService
            .findOneByCredentialsAndAccount(credentialsId, accountId);
        return ErrorHandler.handleFindOne(findOneByCredentials);
    }
}