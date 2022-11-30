package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.repository.CredentialsRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

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
}
