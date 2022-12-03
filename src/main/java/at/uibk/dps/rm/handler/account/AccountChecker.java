package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountService;
import at.uibk.dps.rm.util.PasswordUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class AccountChecker extends EntityChecker {
    private final AccountService accountService;

    private final PasswordUtility passwordUtility;

    public AccountChecker(AccountService accountService) {
        super(accountService);
        this.accountService = accountService;
        passwordUtility = new PasswordUtility();
    }

    public Single<JsonObject> checkFindLoginAccount(String username) {
        Single<JsonObject> findOneByUsername = accountService.findOneByUsername(username);
        return ErrorHandler.handleLoginCredentials(findOneByUsername);
    }

    @Override
    public Completable checkForDuplicateEntity(JsonObject entity) {
        Single<Boolean> existsOneByUsername = accountService.existsOneByUsername(entity.getString("username"), false);
        return ErrorHandler.handleDuplicates(existsOneByUsername).ignoreElement();
    }

    public JsonObject checkComparePasswords(JsonObject account, char[] givenPassword) {
        if (passwordUtility.verifyPassword(account.getString("password"), givenPassword)) {
            return account;
        }
        throw new UnauthorizedException();
    }

    public JsonObject hashAccountPassword(JsonObject account) {
        String hash = passwordUtility.hashPassword(account.getString("password").toCharArray());
        account.put("password", hash);
        return account;
    }
}
