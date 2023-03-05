package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface VPCService extends ServiceInterface {
    @GenIgnore
    static VPCService create(VPCRepository vpcRepository) {
        return new VPCServiceImpl(vpcRepository);
    }

    static VPCService createProxy(Vertx vertx, String address) {
        return new VPCServiceVertxEBProxy(vertx, address);
    }

    Future<JsonObject> findOneByRegionIdAndAccountId(long regionId, long accountId);
}
