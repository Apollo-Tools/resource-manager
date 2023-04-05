package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

public class CredentialsChecker extends EntityChecker {

    private final CredentialsService credentialsService;

    public CredentialsChecker(CredentialsService credentialsService) {
        super(credentialsService);
        this.credentialsService = credentialsService;
    }

    public Single<JsonArray> checkFindAll(long accountId) {
        return ErrorHandler.handleFindAll(credentialsService.findAllByAccountId(accountId));
    }

    public Completable checkExistsOneByProviderId(long accountId, long providerId) {
        Single<Boolean> existsOneByProviderId = credentialsService.existsOnyByAccountIdAndProviderId(accountId, providerId);
        return ErrorHandler.handleCredentialsExist(existsOneByProviderId).ignoreElement();
    }
}
