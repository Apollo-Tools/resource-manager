package at.uibk.dps.rm.service.resource;

import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.service.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

@ProxyGen
public interface ResourceService extends ServiceInterface {
    @GenIgnore
    static ResourceService create(ResourceRepository resourceRepository) {
        return new ResourceServiceImpl(resourceRepository);
    }

    @GenIgnore
    static ResourceService createProxy(Vertx vertx, String address) {
        return new ResourceServiceVertxEBProxy(vertx, address);
    }

    Future<Boolean> existsOneByUrl(String url);

    Future<Boolean> existsOneByResourceType(long typeId);
}
