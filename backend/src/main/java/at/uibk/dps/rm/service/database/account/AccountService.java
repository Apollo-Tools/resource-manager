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

@ProxyGen
@VertxGen
public interface AccountService extends DatabaseServiceInterface {

    @Generated
    @GenIgnore
    static AccountService create(AccountRepository accountRepository) {
        return new AccountServiceImpl(accountRepository);
    }

    @Generated
    static AccountService createProxy(Vertx vertx) {
        return new AccountServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Account.class));
    }

    Future<JsonObject> findOneByUsername(String username);

    Future<Boolean> existsOneByUsername(String username, boolean isActive);
}
