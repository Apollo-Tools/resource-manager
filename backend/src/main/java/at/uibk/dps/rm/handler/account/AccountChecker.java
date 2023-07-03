package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

/**
 * Implements methods to perform CRUD operations on the account entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class AccountChecker extends EntityChecker {
    private final AccountService accountService;

    /**
     * Create an instance from the accountService.
     *
     * @param accountService the account service
     */
    public AccountChecker(AccountService accountService) {
        super(accountService);
        this.accountService = accountService;
    }

    /**
     * Find account by username.
     *
     * @param username the username
     * @return a Single that emits the found account as JsonObject
     */
    public Single<JsonObject> checkLoginAccount(String username, String password) {
        return accountService.loginAccount(username, password);
    }
}
