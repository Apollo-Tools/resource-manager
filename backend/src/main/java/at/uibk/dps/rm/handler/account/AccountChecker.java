package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

/**
 * Implements methods to perform CRUD operations on the account entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
@Deprecated
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

    /**
     * Lock account by its id.
     *
     * @param accountId the id of the account
     */
    public Completable lockAccount(long accountId) {
        return accountService.setAccountActive(accountId, false);
    }

    /**
     * Unlock account by its id.
     *
     * @param accountId the id of the account
     */
    public Completable unlockAccount(long accountId) {
        return accountService.setAccountActive(accountId, true);
    }
}
