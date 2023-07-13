package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

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
    static AccountService create(AccountRepository accountRepository, Stage.SessionFactory sessionFactory) {
        return new AccountServiceImpl(accountRepository, sessionFactory);
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
     * @return a Future that emits the logged in account
     */
    Future<JsonObject> loginAccount(String username, String password);
}
