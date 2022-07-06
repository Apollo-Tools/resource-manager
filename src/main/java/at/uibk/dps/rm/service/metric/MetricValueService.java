package at.uibk.dps.rm.service.metric;

import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface MetricValueService {

    @GenIgnore
    static MetricValueService create(MetricValueRepository metricValueRepository) {
        return new MetricValueServiceImpl(metricValueRepository);
    }

    @GenIgnore
    static MetricValueService createProxy(Vertx vertx, String address) {
        return new MetricValueServiceVertxEBProxy(vertx, address);
    }

    Future<JsonObject> save(JsonObject data);

    Future<JsonObject> findOne(long id);

    Future<Boolean> existsOneByResourceAndMetricId(long resourceId, long MetricId);

    Future<Void> delete(long id);
}
