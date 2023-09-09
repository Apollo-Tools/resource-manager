package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.repository.account.RoleRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

/**
 * The interface of the service proxy for the account entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface AccountService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static AccountService create(AccountRepository accountRepository, RoleRepository roleRepository,
            SessionManagerProvider smProvider) {
        return new AccountServiceImpl(accountRepository, roleRepository, smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static AccountService createProxy(Vertx vertx) {
        return new AccountServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Account.class));
    }

    /**
     * Login the account.
     *
     * @param username the username of the account
     * @param password the password of the account
     * @param resultHandler receives the account if the login was successful else an error
     */
    void loginAccount(String username, String password, Handler<AsyncResult<JsonObject>> resultHandler);

    /**
     * Lock or unlock an account.
     *
     * @param accountId the id of the account
     * @param activityLevel whether to lock or unlock the account
     * @param resultHandler receives nothing if the update was successful else an error
     */
    void setAccountActive(long accountId, boolean activityLevel, Handler<AsyncResult<Void>> resultHandler);
}
