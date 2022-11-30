package at.uibk.dps.rm.service.database.cloudprovider;

import at.uibk.dps.rm.repository.CloudProviderRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface CloudProviderService  extends ServiceInterface {
    @GenIgnore
    static CloudProviderService create(CloudProviderRepository cloudProviderRepository) {
        return new CloudProviderServiceImpl(cloudProviderRepository);
    }

    static CloudProviderService createProxy(Vertx vertx, String address) {
        return new CloudProviderServiceVertxEBProxy(vertx, address);
    }
}
