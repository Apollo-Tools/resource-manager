package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.entity.dto.GetResourceTypeMetricsResponse;
import at.uibk.dps.rm.entity.dto.metric.ResourceTypeMetric;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.resourceprovider.RegionChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResourceTypeMetricHandler extends ValidationHandler {

    private final MetricChecker metricChecker;

    private final RegionChecker regionChecker;

    public ResourceTypeMetricHandler(ServiceProxyProvider serviceProxyProvider) {
        super(new MetricChecker(serviceProxyProvider.getMetricService()));
        metricChecker = (MetricChecker) super.entityChecker;
        regionChecker = new RegionChecker(serviceProxyProvider.getRegionService());
    }

    protected Single<JsonObject> getAllMetrics(RoutingContext rc) {
        GetResourceTypeMetricsResponse response = new GetResourceTypeMetricsResponse();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> regionChecker.checkFindOne(id)
                .map(res -> id))
            .flatMap(id ->
                metricChecker.checkFindAllByResourceTypeId(id, true)
                    .flatMap(requiredMetrics -> {
                        mapResourceTypeMetricsToResponse(requiredMetrics, response, true);
                        return metricChecker.checkFindAllByResourceTypeId(id, false);
                    })
                    .map(optionalMetrics -> {
                        mapResourceTypeMetricsToResponse(optionalMetrics, response, false);
                        return JsonObject.mapFrom(response);
                    })
            );
    }

    private void mapResourceTypeMetricsToResponse(JsonArray metrics, GetResourceTypeMetricsResponse response,
                                                  boolean required) {
        for (Object entity: metrics.getList()) {
            Metric metric = ((JsonObject) entity).mapTo(Metric.class);
            ResourceTypeMetric resourceTypeMetric = new ResourceTypeMetric();
            resourceTypeMetric.setMetric(metric);
            resourceTypeMetric.setRequired(required);
            response.getResourceTypeMetrics().add(resourceTypeMetric);
        }
    }
}
