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

/**
 * The interface of the service proxy for the resource_type entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ResourceTypeService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ResourceTypeService create(ResourceTypeRepository resourceTypeRepository) {
        return new ResourceTypeServiceImpl(resourceTypeRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ResourceTypeService createProxy(Vertx vertx) {
        return new ResourceTypeServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(ResourceType.class));
    }

    /**
     * Check if a resource type exists by its name.
     *
     * @param resourceType the name of the resource type
     * @return a Future that emits true if the resource type exists, else false
     */
    Future<Boolean> existsOneByResourceType(String resourceType);
}
