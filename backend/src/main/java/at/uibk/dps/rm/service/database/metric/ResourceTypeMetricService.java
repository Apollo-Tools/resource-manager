package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ResourceTypeMetric;
import at.uibk.dps.rm.repository.metric.ResourceTypeMetricRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface ResourceTypeMetricService extends DatabaseServiceInterface {

    @Generated
    @GenIgnore
    static ResourceTypeMetricService create(ResourceTypeMetricRepository repository) {
        return new ResourceTypeMetricServiceImpl(repository);
    }

    @Generated
    static ResourceTypeMetricService createProxy(Vertx vertx) {
        return new ResourceTypeMetricServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(ResourceTypeMetric.class));
    }

    Future<Boolean> missingRequiredResourceTypeMetricsByResourceId(long resourceId);
}
