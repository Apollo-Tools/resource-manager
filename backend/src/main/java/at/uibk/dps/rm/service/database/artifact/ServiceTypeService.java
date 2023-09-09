package at.uibk.dps.rm.service.database.artifact;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ServiceType;
import at.uibk.dps.rm.repository.artifact.ServiceTypeRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

/**
 * The interface of the service proxy for the artifact type entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ServiceTypeService extends DatabaseServiceInterface {
    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ServiceTypeService create(ServiceTypeRepository serviceTypeRepository, SessionManagerProvider smProvider) {
        return new ServiceTypeServiceImpl(serviceTypeRepository, smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ServiceTypeService createProxy(Vertx vertx) {
        return new ServiceTypeServiceVertxEBProxy(vertx,
                ServiceProxyAddress.getServiceProxyAddress(ServiceType.class));
    }
}
