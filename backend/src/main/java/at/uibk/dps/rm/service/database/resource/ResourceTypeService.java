package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.repository.resource.ResourceTypeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;


@ProxyGen
@VertxGen
public interface ResourceTypeService extends DatabaseServiceInterface {

    @Generated
    @GenIgnore
    static ResourceTypeService create(ResourceTypeRepository resourceTypeRepository) {
        return new ResourceTypeServiceImpl(resourceTypeRepository);
    }

    @Generated
    static ResourceTypeService createProxy(Vertx vertx) {
        return new ResourceTypeServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(ResourceType.class));
    }

    Future<Boolean> existsOneByResourceType(String resourceType);
}
