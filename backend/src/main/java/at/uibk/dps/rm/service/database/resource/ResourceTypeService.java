package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.repository.resource.ResourceTypeRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;


@ProxyGen
@VertxGen
public interface ResourceTypeService extends ServiceInterface {
    @GenIgnore
    static ResourceTypeService create(ResourceTypeRepository resourceTypeRepository) {
        return new ResourceTypeServiceImpl(resourceTypeRepository);
    }

    static ResourceTypeService createProxy(Vertx vertx, String address) {
        return new ResourceTypeServiceVertxEBProxy(vertx, address);
    }

    Future<Boolean> existsOneByResourceType(String resourceType);
}
