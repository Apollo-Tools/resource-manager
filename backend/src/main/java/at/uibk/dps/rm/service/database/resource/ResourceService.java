package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

import java.util.List;

@ProxyGen
@VertxGen
public interface ResourceService extends ServiceInterface {
    @GenIgnore
    static ResourceService create(ResourceRepository resourceRepository) {
        return new ResourceServiceImpl(resourceRepository);
    }

    static ResourceService createProxy(Vertx vertx, String address) {
        return new ResourceServiceVertxEBProxy(vertx, address);
    }

    Future<JsonArray> findAllUnreserved();

    Future<JsonArray> checkFindAllBySLOs(long functionId, List<String> metrics,
                                                          List<String> regions, List<Long> providerIds,
                                                          List<Long> resourceTypeIds);

    Future<JsonArray> findAllByFunctionId(long functionId);

    Future<Boolean> existsOneByResourceType(long typeId);
}
