package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

/**
 * The interface of the service proxy for the region entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface RegionService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static RegionService create(RegionRepository regionRepository) {
        return new RegionServiceImpl(regionRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static RegionService createProxy(Vertx vertx) {
        return new RegionServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Region.class));
    }

    /**
     * Find all regions that belong to the resource provider.
     *
     * @param providerId the id of the resource provider
     * @return a Future that emits all resource providers as JsonArray
     */
    Future<JsonArray> findAllByProviderId(long providerId);

    /**
     * Check if a region exists by its name and resource provider.
     *
     * @param name the name of the region
     * @param providerId the id of the resource provider
     * @return a Future that emits true if the region exists, else false
     */
    Future<Boolean> existsOneByNameAndProviderId(String name, long providerId);
}
