package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

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
    static AccountService create(AccountRepository accountRepository) {
        return new AccountServiceImpl(accountRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static AccountService createProxy(Vertx vertx) {
        return new AccountServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Account.class));
    }

    /**
     * Find an account by its username.
     *
     * @param username the username of the account
     * @return a Future that emits the account as JsonObject if it exists, else null
     */
    Future<JsonObject> findOneByUsername(String username);

    /**
     * Check if an account exists by its username and active status.
     *
     * @param username the username of the account
     * @param isActive the active status of the account
     * @return a Future that emits true if the account exists, else false
     */
    Future<Boolean> existsOneByUsername(String username, boolean isActive);
}
