package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

/**
 * The interface of the service proxy for the metric_value entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface MetricValueService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static MetricValueService create(SessionManagerProvider smProvider) {
        return new MetricValueServiceImpl(new MetricValueRepository(), new PlatformMetricRepository(), smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static MetricValueService createProxy(Vertx vertx) {
        return new MetricValueServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(MetricValue.class));
    }

    /**
     * Save all metric values from data to a resource.
     *
     * @param resourceId the id of the resource
     * @param data the metric value data to set
     * @param resultHandler receives nothing if the saving was successful else an error
     */
    void saveAllToResource(long resourceId, JsonArray data, Handler<AsyncResult<Void>> resultHandler);

    /**
     * Find all metric values by their resource and add the values based on includeValue.
     *
     * @param resourceId the id of the resource
     * @param includeValue whether to include the values
     * @param resultHandler receives the found metric values as JsonArray
     */
    void findAllByResource(long resourceId, boolean includeValue, Handler<AsyncResult<JsonArray>> resultHandler);

    /**
     * Update a metric value based on its resource and metric. Set the new value using the
     * valueString, valueNumber and valueBool. Only one of the three values should be non-null.
     *
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @param valueString the new string value
     * @param valueNumber the new number value
     * @param valueBool the new boolean value
     * @param resultHandler receives nothing if the update was successful else an error
     */
    void updateByResourceAndMetric(long resourceId, long metricId, String valueString, Double valueNumber,
        Boolean valueBool, boolean isExternalSource, Handler<AsyncResult<Void>> resultHandler);

    /**
     * Delete a metric value by its resource and metric.
     *
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @param resultHandler receives nothing if the deletíon was successful else an error
     */
    void deleteByResourceAndMetric(long resourceId, long metricId, Handler<AsyncResult<Void>> resultHandler);
}
