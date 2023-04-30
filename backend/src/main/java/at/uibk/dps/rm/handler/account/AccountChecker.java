package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountService;
import at.uibk.dps.rm.util.misc.PasswordUtility;
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
public class AccountChecker extends EntityChecker {
    private final AccountService accountService;

    private final PasswordUtility passwordUtility;

    /**
     * Create an instance from the accountService.
     *
     * @param accountService the account service
     */
    public AccountChecker(AccountService accountService) {
        super(accountService);
        this.accountService = accountService;
        this.passwordUtility = new PasswordUtility();
    }

    /**
     * Find account by username.
     *
     * @param username the username
     * @return a Single that emits the found account as JsonObject
     */
    public Single<JsonObject> checkFindLoginAccount(String username) {
        Single<JsonObject> findOneByUsername = accountService.findOneByUsername(username);
        return ErrorHandler.handleLoginCredentials(findOneByUsername);
    }

    @Override
    public Completable checkForDuplicateEntity(JsonObject entity) {
        Single<Boolean> existsOneByUsername = accountService.existsOneByUsername(entity.getString("username"), false);
        return ErrorHandler.handleDuplicates(existsOneByUsername).ignoreElement();
    }

    /**
     * Compare the persisted password with a given password
     *
     * @param account the persisted account
     * @param givenPassword the given password
     * @return the account if the two passwords are equal, else throw new UnauthorizedException
     */
    public JsonObject checkComparePasswords(JsonObject account, char[] givenPassword) {
        if (passwordUtility.verifyPassword(account.getString("password"), givenPassword)) {
            return account;
        }
        throw new UnauthorizedException();
    }

    /**
     * Hash a password.
     *
     * @param account account of whom the password has to be hashed
     * @return the account where the original password was swapped with the hashed password
     */
    public JsonObject hashAccountPassword(JsonObject account) {
        String hash = passwordUtility.hashPassword(account.getString("password").toCharArray());
        account.put("password", hash);
        return account;
    }
}
