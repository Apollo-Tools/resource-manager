package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface AccountService extends ServiceInterface {

    @Generated
    @GenIgnore
    static AccountService create(AccountRepository accountRepository) {
        return new AccountServiceImpl(accountRepository);
    }

    @Generated
    static AccountService createProxy(Vertx vertx, String address) {
        return new AccountServiceVertxEBProxy(vertx, address);
    }

    Future<JsonObject> findOneByUsername(String username);

    Future<Boolean> existsOneByUsername(String username, boolean isActive);
}
