package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface MetricService extends DatabaseServiceInterface {

    @Generated
    @GenIgnore
    static MetricService create(MetricRepository metricRepository) {
        return new MetricServiceImpl(metricRepository);
    }

    @Generated
    static MetricService createProxy(Vertx vertx) {
        return new MetricServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Metric.class));
    }

    Future<JsonArray> findAllByResourceTypeId(long resourceTypeId, boolean required);

    Future<JsonObject> findOneByMetric(String metric);

    Future<Boolean> existsOneByMetric(String metric);
}
