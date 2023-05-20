package at.uibk.dps.rm.service.database.service;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ServiceType;
import at.uibk.dps.rm.repository.service.ServiceTypeRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

/**
 * The interface of the service proxy for the service_type entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ServiceTypeService extends DatabaseServiceInterface {
    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ServiceTypeService create(ServiceTypeRepository serviceTypeRepository) {
        return new ServiceTypeServiceImpl(serviceTypeRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ServiceTypeService createProxy(Vertx vertx) {
        return new ServiceTypeServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(ServiceType.class));
    }
}
