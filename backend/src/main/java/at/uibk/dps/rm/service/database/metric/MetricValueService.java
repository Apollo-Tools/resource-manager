package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import org.hibernate.reactive.stage.Stage;

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
    static MetricValueService create(MetricValueRepository metricValueRepository, Stage.SessionFactory sessionFactory) {
        return new MetricValueServiceImpl(metricValueRepository, sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static MetricValueService createProxy(Vertx vertx) {
        return new MetricValueServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(MetricValue.class));
    }

    /**
     * Find all metric values by their resource and add the values based on includeValue.
     *
     * @param resourceId the id of the resource
     * @param includeValue whether to include the values
     * @return a Future that emits all metric values as JsonArray
     */
    Future<JsonArray> findAllByResource(long resourceId, boolean includeValue);

    /**
     * Check if a metric value exists by its resource and metric.
     *
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @return a Future that emits true if the metric value exists, else false
     */
    Future<Boolean> existsOneByResourceAndMetric(long resourceId, long metricId);

    /**
     * Update a metric value based on its resource and metric. Set the new value using the
     * valueString, valueNumber and valueBool. Only one of the three values should be non-null.
     *
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @param valueString the new string value
     * @param valueNumber the new number value
     * @param valueBool the new boolean value
     * @return an empty Future
     */
    Future<Void> updateByResourceAndMetric(long resourceId, long metricId, String valueString, Double valueNumber,
        Boolean valueBool, boolean isExternalSource);

    /**
     * Delete a metric value by its resource and metric.
     *
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @return an empty Future
     */
    Future<Void> deleteByResourceAndMetric(long resourceId, long metricId);
}
