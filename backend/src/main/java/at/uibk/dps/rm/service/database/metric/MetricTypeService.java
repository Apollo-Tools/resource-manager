package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.MetricType;
import at.uibk.dps.rm.repository.metric.MetricTypeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface MetricTypeService extends DatabaseServiceInterface {

    @Generated
    @GenIgnore
    static MetricTypeService create(MetricTypeRepository metricTypeRepository) {
        return new MetricTypeServiceImpl(metricTypeRepository);
    }

    @Generated
    static MetricTypeService createProxy(Vertx vertx) {
        return new MetricTypeServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(MetricType.class));
    }
}
