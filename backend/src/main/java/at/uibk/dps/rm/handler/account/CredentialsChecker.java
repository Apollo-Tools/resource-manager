package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

public class CredentialsChecker extends EntityChecker {

    private final CredentialsService credentialsService;

    public CredentialsChecker(CredentialsService credentialsService) {
        super(credentialsService);
        this.credentialsService = credentialsService;
    }

    public Single<JsonArray> checkFindAll(long accountId) {
        return credentialsService.findAllByAccountId(accountId);
    }
}
