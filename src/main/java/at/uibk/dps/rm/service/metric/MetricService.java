package at.uibk.dps.rm.service.metric;

import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.service.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

@ProxyGen
public interface MetricService extends ServiceInterface {
    @GenIgnore
    static MetricService create(MetricRepository metricRepository) {
        return new MetricServiceImpl(metricRepository);
    }

    @GenIgnore
    static MetricService createProxy(Vertx vertx, String address) {
        return new MetricServiceVertxEBProxy(vertx, address);
    }

    Future<Boolean> existsOneByMetric(String url);
}
