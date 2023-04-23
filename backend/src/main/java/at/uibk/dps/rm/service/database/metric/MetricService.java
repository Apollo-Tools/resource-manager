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

/**
 * The interface of the service proxy for te metric entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface MetricService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static MetricService create(MetricRepository metricRepository) {
        return new MetricServiceImpl(metricRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static MetricService createProxy(Vertx vertx) {
        return new MetricServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Metric.class));
    }

    /**
     * Find all metrics that belong to a resource type and are required or optional based on the
     * value of required.
     *
     * @param resourceTypeId the type of the resource type
     * @param required wether required or optional metrics should be returned
     * @return a Future that emits all metrics as JsonArray
     */
    Future<JsonArray> findAllByResourceTypeId(long resourceTypeId, boolean required);

    /**
     * Find a metric by its name.
     *
     * @param metric the name of the metric
     * @return a Future that emits the metric as JsonObject if it exists, else null
     */
    Future<JsonObject> findOneByMetric(String metric);

    /**
     * Check if a metric exists by its name.
     *
     * @param metric the name of the metric
     * @return a Future that emits true if the metric exists, else false
     */
    Future<Boolean> existsOneByMetric(String metric);
}
