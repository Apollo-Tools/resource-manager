package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.repository.AccountRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface AccountService extends ServiceInterface {
    @GenIgnore
    static AccountService create(AccountRepository accountRepository) {
        return new AccountServiceImpl(accountRepository);
    }

    static AccountService createProxy(Vertx vertx, String address) {
        return new AccountServiceVertxEBProxy(vertx, address);
    }
}
