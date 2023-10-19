package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.repository.resource.ResourceTypeRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

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
    static ResourceTypeService create(ResourceTypeRepository resourceTypeRepository, SessionManagerProvider smProvider) {
        return new ResourceTypeServiceImpl(resourceTypeRepository, smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ResourceTypeService createProxy(Vertx vertx) {
        return new ResourceTypeServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(ResourceType.class));
    }
}
