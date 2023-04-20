package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.entity.dto.metric.ResourceTypeMetric;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.resource.ResourceTypeChecker;
import at.uibk.dps.rm.util.HttpHelper;
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
public class ResourceTypeMetricHandler extends ValidationHandler {

    private final MetricChecker metricChecker;

    private final ResourceTypeChecker resourceTypeChecker;

    /**
     * Create an instance from the metricChecker and resourceTypeChecker.
     *
     * @param metricChecker the metric checker
     * @param resourceTypeChecker the resource type checker
     */
    public ResourceTypeMetricHandler(MetricChecker metricChecker, ResourceTypeChecker resourceTypeChecker) {
        super(metricChecker);
        this.metricChecker = metricChecker;
        this.resourceTypeChecker = resourceTypeChecker;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        List<ResourceTypeMetric> response = new ArrayList<>();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> resourceTypeChecker.checkFindOne(id)
                .map(res -> id))
            .flatMap(id ->
                metricChecker.checkFindAllByResourceTypeId(id, true)
                    .flatMap(requiredMetrics -> {
                        mapResourceTypeMetricsToResponse(requiredMetrics, response, true);
                        return metricChecker.checkFindAllByResourceTypeId(id, false);
                    })
                    .map(optionalMetrics -> {
                        mapResourceTypeMetricsToResponse(optionalMetrics, response, false);
                        return response;
                    })
                    .map(res ->{
                        JsonArray result = new JsonArray();
                        for (ResourceTypeMetric entity : res) {
                            result.add(JsonObject.mapFrom(entity));
                        }
                        return result;
                    })
            );
    }

    /**
     * Map the metrics to the list of resourceTypeMetrics that is used for the response.
     *
     * @param metrics the metrics
     * @param response the resulting resource type metrics
     * @param required if the resource type metrics are required or optional
     */
    private void mapResourceTypeMetricsToResponse(JsonArray metrics, List<ResourceTypeMetric> response,
                                                  boolean required) {
        for (Object entity: metrics.getList()) {
            Metric metric = ((JsonObject) entity).mapTo(Metric.class);
            ResourceTypeMetric resourceTypeMetric = new ResourceTypeMetric();
            resourceTypeMetric.setMetric(metric);
            resourceTypeMetric.setRequired(required);
            response.add(resourceTypeMetric);
        }
    }
}
