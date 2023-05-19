package at.uibk.dps.rm.service.database.service;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.Set;

/**
 * The interface of the service proxy for the service entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ServiceService extends DatabaseServiceInterface {
    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ServiceService create(ServiceRepository serviceRepository) {
        return new ServiceServiceImpl(serviceRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ServiceService createProxy(Vertx vertx) {
        return new ServiceServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(Service.class));
    }

    /**
     * Check if a service exists by name.
     *
     * @param name the name of the service
     * @return a Future that emits true if it exists, else false
     */
    Future<Boolean> existsOneByName(String name);

    /**
     * Check if all services exists by serviceIds.
     *
     * @param serviceIds the list of service ids
     * @return a Future that emits true if all services exist, else false
     */
    Future<Boolean> existsAllByIds(Set<Long> serviceIds);
}
