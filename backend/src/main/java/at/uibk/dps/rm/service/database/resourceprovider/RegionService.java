package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
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
public interface RegionService extends DatabaseServiceInterface {

    @Generated
    @GenIgnore
    static RegionService create(RegionRepository regionRepository) {
        return new RegionServiceImpl(regionRepository);
    }

    @Generated
    static RegionService createProxy(Vertx vertx) {
        return new RegionServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Region.class));
    }

    Future<JsonArray> findAllByProviderId(long providerId);

    Future<Boolean> existsOneByNameAndProviderId(String name, long providerId);
}
