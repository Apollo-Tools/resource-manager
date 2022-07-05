package at.uibk.dps.rm.service.metric;

import at.uibk.dps.rm.repository.metric.MetricRepository;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface MetricService {
    @GenIgnore
    static MetricService create(MetricRepository metricRepository) {
        return new MetricServiceImpl(metricRepository);
    }

    @GenIgnore
    static MetricService createProxy(Vertx vertx, String address) {
        return new MetricServiceVertxEBProxy(vertx, address);
    }

    Future<JsonObject> save(JsonObject data);

    Future<JsonObject> findOne(long id);

    Future<Boolean> existsOneById(long id);

    Future<Boolean> existsOneByMetric(String url);

    Future<JsonArray> findAll();

    Future<Void> update(JsonObject data);

    Future<Void> delete(long id);
}
