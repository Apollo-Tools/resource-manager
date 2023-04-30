package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * The interface of the service proxy for the resource_provider entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ResourceProviderService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ResourceProviderService create(ResourceProviderRepository resourceProviderRepository) {
        return new ResourceProviderServiceImpl(resourceProviderRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ResourceProviderService createProxy(Vertx vertx) {
        return new ResourceProviderServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(ResourceProvider.class));
    }

    /**
     * Check if a resource provider exists by its name.
     *
     * @param provider the name of the provider
     * @return a Future that emits true if the resource provider exists, else false
     */
    Future<Boolean> existsOneByProvider(String provider);
}
