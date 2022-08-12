package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.repository.MetricValueRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

@ProxyGen
@VertxGen
public interface MetricValueService extends ServiceInterface {

    @GenIgnore
    static MetricValueService create(MetricValueRepository metricValueRepository) {
        return new MetricValueServiceImpl(metricValueRepository);
    }

    static MetricValueService createProxy(Vertx vertx, String address) {
        return new MetricValueServiceVertxEBProxy(vertx, address);
    }

    Future<JsonArray> findAllByResource(long resourceId);

    Future<Boolean> existsOneByResourceAndMetric(long resourceId, long metricId);

    Future<Void> deleteByResourceAndMetric(long resourceId, long metricId);
}
