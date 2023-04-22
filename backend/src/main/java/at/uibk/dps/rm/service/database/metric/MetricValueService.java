package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
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
public interface MetricValueService extends DatabaseServiceInterface {

    @Generated
    @GenIgnore
    static MetricValueService create(MetricValueRepository metricValueRepository) {
        return new MetricValueServiceImpl(metricValueRepository);
    }

    @Generated
    static MetricValueService createProxy(Vertx vertx) {
        return new MetricValueServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(MetricValue.class));
    }

    Future<JsonArray> findAllByResource(long resourceId, boolean includeValue);

    Future<JsonObject> findOneByResourceAndMetric(long resourceId, long metricId);

    Future<Boolean> existsOneByResourceAndMetric(long resourceId, long metricId);

    Future<Void> updateByResourceAndMetric(long resourceId, long metricId, String valueString, Double valueNumber,
                                           Boolean valueBool);

    Future<Void> deleteByResourceAndMetric(long resourceId, long metricId);
}
