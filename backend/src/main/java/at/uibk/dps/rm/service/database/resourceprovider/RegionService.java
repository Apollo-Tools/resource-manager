package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

@ProxyGen
@VertxGen
public interface RegionService extends ServiceInterface {

    @GenIgnore
    static RegionService create(RegionRepository regionRepository) {
        return new RegionServiceImpl(regionRepository);
    }

    static RegionService createProxy(Vertx vertx, String address) {
        return new RegionServiceVertxEBProxy(vertx, address);
    }

    Future<JsonArray> findAllByProviderId(long providerId);

    Future<Boolean> existsOneByNameAndProviderId(String name, long providerId);
}
