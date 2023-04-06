package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.repository.account.AccountCredentialsRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface AccountCredentialsService extends ServiceInterface {

    @Generated
    @GenIgnore
    static AccountCredentialsService create(AccountCredentialsRepository accountCredentialsRepository) {
        return new AccountCredentialsServiceImpl(accountCredentialsRepository);
    }

    @Generated
    static AccountCredentialsService createProxy(Vertx vertx, String address) {
        return new AccountCredentialsServiceVertxEBProxy(vertx, address);
    }

    Future<JsonObject> findOneByCredentialsAndAccount(long credentialsId, long accountId);

    Future<Boolean> existsOneByAccountAndProvider(long accountId, long providerId);
}
