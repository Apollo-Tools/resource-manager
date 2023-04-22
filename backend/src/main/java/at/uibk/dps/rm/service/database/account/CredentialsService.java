package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

@ProxyGen
@VertxGen
public interface CredentialsService extends DatabaseServiceInterface {

    @Generated
    @GenIgnore
    static CredentialsService create(CredentialsRepository credentialsRepository) {
        return new CredentialsServiceImpl(credentialsRepository);
    }

    @Generated
    static CredentialsService createProxy(Vertx vertx) {
        return new CredentialsServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Credentials.class));
    }

    Future<JsonArray> findAllByAccountId(long accountId);

    Future<Boolean> existsAtLeastOneByAccount(long accountId);

    Future<Boolean> existsOnyByAccountIdAndProviderId(long accountId, long providerId);
}
