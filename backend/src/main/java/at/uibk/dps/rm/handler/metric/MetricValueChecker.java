package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricValueService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Implements methods to perform CRUD operations on the metric_value entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class MetricValueChecker extends EntityChecker {

    private final MetricValueService metricValueService;

    /**
     * Create an instance from the metricValueService.
     *
     * @param metricValueService the metric value service
     */
    public MetricValueChecker(MetricValueService metricValueService) {
        super(metricValueService);
        this.metricValueService = metricValueService;
    }

    /**
     * Find all metric values by resource.
     *
     * @param resourceId the id of the resource
     * @param includeValue whether to include the value in the result or not
     * @return a Single that emits all found entities as JsonArray
     */
    public Single<JsonArray> checkFindAllByResource(long resourceId, boolean includeValue) {
        Single<JsonArray> findAllByResource = metricValueService.findAllByResource(resourceId, includeValue);
        return ErrorHandler.handleFindAll(findAllByResource);
    }

    /**
     * Submit the update of a metric value.
     *
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @param value the new value
     * @return a Completable
     */
    public Completable submitUpdateMetricValue(long resourceId, long metricId, String value) {
        return metricValueService.updateByResourceAndMetric(resourceId, metricId, value, null,
            null, true);
    }

    /**
     * @see #submitUpdateMetricValue(long, long, String)
     */
    public Completable submitUpdateMetricValue(long resourceId, long metricId, Double value) {
        return metricValueService.updateByResourceAndMetric(resourceId, metricId, null, value, null,
            true);
    }

    /**
     * @see #submitUpdateMetricValue(long, long, String)
     */
    public Completable submitUpdateMetricValue(long resourceId, long metricId, Boolean value) {
        return metricValueService.updateByResourceAndMetric(resourceId, metricId, null, null,
            value, true);
    }

    /**
     * Submit the deletion of a metric value by resource and metric.
     *
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @return a Completable
     */
    public Completable submitDeleteMetricValue(long resourceId, long metricId) {
        return metricValueService.deleteByResourceAndMetric(resourceId, metricId);
    }

    /**
     * Check if the creation of a metric value would create a duplicate.
     *
     * @param resourceId the id of the resource
     * @param metricId  the id of the metric
     * @return a Completable
     */
    public Completable checkForDuplicateByResourceAndMetric(long resourceId, long metricId) {
        final Single<Boolean> existsOneByResourceAndMetric = metricValueService.existsOneByResourceAndMetric(resourceId,
            metricId);
        return ErrorHandler.handleDuplicates(existsOneByResourceAndMetric).ignoreElement();
    }

    /**
     * Check if a metric value exists by resource and metric
     *
     * @param resourceId the id of the resource
     * @param metricId  the id of the metric
     * @return a Completable
     */
    public Completable checkMetricValueExistsByResourceAndMetric(long resourceId, long metricId) {
        Single<Boolean> existsOneByResourceAndMetric =
            metricValueService.existsOneByResourceAndMetric(resourceId, metricId);
        return ErrorHandler.handleExistsOne(existsOneByResourceAndMetric).ignoreElement();
    }

    /**
     * Update a metric value depending on its type.
     *
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @param requestBody the request body
     * @return a Completable
     */
    public Completable updateOneByValue(long resourceId, long metricId, JsonObject requestBody) {
        Object value = requestBody.getValue("value");
        if (value instanceof String) {
            return this.submitUpdateMetricValue(resourceId, metricId, (String) value);
        } else if (value instanceof Number) {
            return this.submitUpdateMetricValue(resourceId, metricId, ((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            return this.submitUpdateMetricValue(resourceId, metricId, (Boolean) value);
        }
        return Completable.error(new BadInputException());
    }
}
