package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.repository.metric.MetricTypeRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface MetricTypeService extends ServiceInterface {
    @GenIgnore
    static MetricTypeService create(MetricTypeRepository metricTypeRepository) {
        return new MetricTypeServiceImpl(metricTypeRepository);
    }

    static MetricTypeService createProxy(Vertx vertx, String address) {
        return new MetricTypeServiceVertxEBProxy(vertx, address);
    }
}
