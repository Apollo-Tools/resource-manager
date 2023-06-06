package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.entity.dto.metric.PlatformMetric;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.resource.PlatformChecker;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Processes the http requests that concern the resource_type_metric entity.
 *
 * @author matthi-g
 */
public class PlatformMetricHandler extends ValidationHandler {

    private final MetricChecker metricChecker;

    private final PlatformChecker platformChecker;

    /**
     * Create an instance from the metricChecker and resourceTypeChecker.
     *
     * @param metricChecker the metric checker
     * @param platformChecker the platform checker
     */
    public PlatformMetricHandler(MetricChecker metricChecker, PlatformChecker platformChecker) {
        super(metricChecker);
        this.metricChecker = metricChecker;
        this.platformChecker = platformChecker;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        List<PlatformMetric> response = new ArrayList<>();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> platformChecker.checkFindOne(id)
                .map(res -> id))
            .flatMap(id ->
                metricChecker.checkFindAllByPlatform(id, true)
                    .flatMap(requiredMetrics -> {
                        mapPlatformMetricsToResponse(requiredMetrics, response, true);
                        return metricChecker.checkFindAllByPlatform(id, false);
                    })
                    .map(optionalMetrics -> {
                        mapPlatformMetricsToResponse(optionalMetrics, response, false);
                        return response;
                    })
                    .map(res ->{
                        JsonArray result = new JsonArray();
                        for (PlatformMetric entity : res) {
                            result.add(JsonObject.mapFrom(entity));
                        }
                        return result;
                    })
            );
    }

    /**
     * Map the metrics to the list of platformMetrics that is used for the response.
     *
     * @param metrics the metrics
     * @param response the resulting platform metrics
     * @param required if the platform metrics are required or optional
     */
    private void mapPlatformMetricsToResponse(JsonArray metrics, List<PlatformMetric> response,
                                                  boolean required) {
        for (Object entity: metrics.getList()) {
            Metric metric = ((JsonObject) entity).mapTo(Metric.class);
            PlatformMetric platformMetric = new PlatformMetric();
            platformMetric.setMetric(metric);
            platformMetric.setRequired(required);
            response.add(platformMetric);
        }
    }
}
