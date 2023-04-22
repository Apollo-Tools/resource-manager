package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface ResourceProviderService extends DatabaseServiceInterface {

    @Generated
    @GenIgnore
    static ResourceProviderService create(ResourceProviderRepository resourceProviderRepository) {
        return new ResourceProviderServiceImpl(resourceProviderRepository);
    }

    @Generated
    static ResourceProviderService createProxy(Vertx vertx) {
        return new ResourceProviderServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(ResourceProvider.class));
    }

    Future<Boolean> existsOneByProvider(String provider);
}
