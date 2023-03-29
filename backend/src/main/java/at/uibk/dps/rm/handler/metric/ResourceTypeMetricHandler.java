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

public class ResourceTypeMetricHandler extends ValidationHandler {

    private final MetricChecker metricChecker;

    private final ResourceTypeChecker resourceTypeChecker;

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
