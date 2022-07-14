package at.uibk.dps.rm.service.metric;

import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.service.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

@ProxyGen
public interface MetricValueService extends ServiceInterface {

    @GenIgnore
    static MetricValueService create(MetricValueRepository metricValueRepository) {
        return new MetricValueServiceImpl(metricValueRepository);
    }

    @GenIgnore
    static MetricValueService createProxy(Vertx vertx, String address) {
        return new MetricValueServiceVertxEBProxy(vertx, address);
    }

    Future<Void> saveAll(JsonArray data);

    Future<JsonArray> findAllByResource(long resourceId);

    Future<Boolean> existsOneByResourceAndMetric(long resourceId, long metricId);

    Future<Void> deleteByResourceAndMetric(long resourceId, long metricId);
}
