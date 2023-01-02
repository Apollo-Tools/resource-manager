package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.repository.ResourceProviderRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface ResourceProviderService extends ServiceInterface {
    @GenIgnore
    static ResourceProviderService create(ResourceProviderRepository resourceProviderRepository) {
        return new ResourceProviderServiceImpl(resourceProviderRepository);
    }

    static ResourceProviderService createProxy(Vertx vertx, String address) {
        return new ResourceProviderServiceVertxEBProxy(vertx, address);
    }

    Future<Boolean> existsOneByProvider(String provider);
}
