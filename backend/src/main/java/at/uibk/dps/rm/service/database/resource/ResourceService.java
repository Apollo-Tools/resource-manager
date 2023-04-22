package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

import java.util.List;

@ProxyGen
@VertxGen
public interface ResourceService extends DatabaseServiceInterface {

    @Generated
    @GenIgnore
    static ResourceService create(ResourceRepository resourceRepository) {
        return new ResourceServiceImpl(resourceRepository);
    }

    @Generated
    static ResourceService createProxy(Vertx vertx) {
        return new ResourceServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Resource.class));
    }

    Future<JsonArray> findAllBySLOs(long functionId, List<String> metrics,
                                                          List<String> regions, List<Long> providerIds,
                                                          List<Long> resourceTypeIds);

    Future<JsonArray> findAllByFunctionId(long functionId);

    Future<Boolean> existsOneByResourceType(long typeId);
}
