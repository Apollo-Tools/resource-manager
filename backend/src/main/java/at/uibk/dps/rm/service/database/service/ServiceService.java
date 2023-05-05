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

@ProxyGen
@VertxGen
public interface ServiceService extends DatabaseServiceInterface {
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

    Future<Boolean> existsOneByName(String name);
}
