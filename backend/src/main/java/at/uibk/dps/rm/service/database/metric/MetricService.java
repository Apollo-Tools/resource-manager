package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface MetricService extends ServiceInterface {
    @GenIgnore
    static MetricService create(MetricRepository metricRepository) {
        return new MetricServiceImpl(metricRepository);
    }

    static MetricService createProxy(Vertx vertx, String address) {
        return new MetricServiceVertxEBProxy(vertx, address);
    }

    Future<JsonArray> findAllByResourceTypeId(long resourceTypeId, boolean required);

    Future<JsonObject> findOneByMetric(String metric);

    Future<Boolean> existsOneByMetric(String metric);
}
