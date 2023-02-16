package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

@ProxyGen
@VertxGen
public interface CredentialsService extends ServiceInterface {
    @GenIgnore
    static CredentialsService create(CredentialsRepository credentialsRepository) {
        return new CredentialsServiceImpl(credentialsRepository);
    }

    static CredentialsService createProxy(Vertx vertx, String address) {
        return new CredentialsServiceVertxEBProxy(vertx, address);
    }

    Future<JsonArray> findAllByAccountId(long accountId);

    Future<Boolean> existsAtLeastOneByAccount(long accountId);
}
