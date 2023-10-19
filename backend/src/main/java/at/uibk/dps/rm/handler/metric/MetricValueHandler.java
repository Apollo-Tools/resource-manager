package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricValueService;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the metric_value entity.
 *
 * @author matthi-g
 */
public class MetricValueHandler extends ValidationHandler {

    private final MetricValueService metricValueService;

    /**
     * Create an instance from the metricValueService.
     *
     * @param metricValueService the service
     */
    public MetricValueHandler(MetricValueService metricValueService) {
        super(metricValueService);
        this.metricValueService = metricValueService;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> metricValueService.findAllByResource(id, true));
    }

    @Override
    public Completable postAll(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMapCompletable(id -> metricValueService.saveAllToResource(id, requestBody));
    }

    @Override
    public Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "resourceId")
            .flatMapCompletable(resourceId -> HttpHelper.getLongPathParam(rc, "metricId")
                .flatMapCompletable(metricId -> this.updateOneByValue(resourceId, metricId, requestBody))
            );
    }

    @Override
    public Completable deleteOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "resourceId")
            .flatMapCompletable(resourceId -> HttpHelper.getLongPathParam(rc, "metricId")
                .flatMapCompletable(metricId -> metricValueService.deleteByResourceAndMetric(resourceId, metricId))
            );
    }


    /**
     * Submit the update of a metric value.
     *
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @param value the new value
     * @return a Completable
     */
    private Completable submitUpdateMetricValue(long resourceId, long metricId, String value) {
        return metricValueService.updateByResourceAndMetric(resourceId, metricId, value, null,
            null, true);
    }

    /**
     * @see #submitUpdateMetricValue(long, long, String)
     */
    private Completable submitUpdateMetricValue(long resourceId, long metricId, Double value) {
        return metricValueService.updateByResourceAndMetric(resourceId, metricId, null, value, null,
            true);
    }

    /**
     * @see #submitUpdateMetricValue(long, long, String)
     */
    private Completable submitUpdateMetricValue(long resourceId, long metricId, Boolean value) {
        return metricValueService.updateByResourceAndMetric(resourceId, metricId, null, null,
            value, true);
    }

    /**
     * Update a metric value depending on its type.
     *
     * @param resourceId the id of the resource
     * @param metricId the id of the metric
     * @param requestBody the request body
     * @return a Completable
     */
    private Completable updateOneByValue(long resourceId, long metricId, JsonObject requestBody) {
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
