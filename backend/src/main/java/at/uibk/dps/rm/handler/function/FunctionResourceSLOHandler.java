package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.entity.dto.GetResourcesBySLOsRequest;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.handler.metric.MetricChecker;
import at.uibk.dps.rm.handler.metric.MetricValueChecker;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.HttpHelper;
import at.uibk.dps.rm.util.SLOCompareUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionResourceSLOHandler {

    private final FunctionChecker functionChecker;

    private final ResourceChecker resourceChecker;

    private final MetricChecker metricChecker;

    private final MetricValueChecker metricValueChecker;

    public FunctionResourceSLOHandler(ServiceProxyProvider serviceProxyProvider) {
        this.functionChecker = new FunctionChecker(serviceProxyProvider.getFunctionService());
        this.resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        this.metricChecker = new MetricChecker(serviceProxyProvider.getMetricService());
        this.metricValueChecker = new MetricValueChecker(serviceProxyProvider.getMetricValueService());
    }

    public Single<JsonArray> getFunctionResourceBySLOs(RoutingContext rc) {
        GetResourcesBySLOsRequest requestDTO = rc.body()
            .asJsonObject()
            .mapTo(GetResourcesBySLOsRequest.class);
        List<ServiceLevelObjective> serviceLevelObjectives = requestDTO.getServiceLevelObjectives();
        List<Completable> completables = new ArrayList<>();
        serviceLevelObjectives.forEach(slo -> completables.add(metricChecker.checkServiceLevelObjectives(slo)));
        return Completable.merge(completables)
            .andThen(HttpHelper.getLongPathParam(rc, "id"))
            .flatMap(functionChecker::checkFindOne)
            .flatMap(function -> Observable.fromStream(serviceLevelObjectives.stream())
                .map(ServiceLevelObjective::getName)
                .toList()
                .flatMap(metrics -> resourceChecker.checkFindAllByMultipleMetrics(function.getLong("function_id"),
                    metrics)))
            .flatMap(this::mapMetricValuesToResources)
            .map(this::mapJsonListToResourceList)
            .map(resources -> filterAndSortResultList(resources, serviceLevelObjectives, requestDTO.getLimit()));
    }



    protected Single<List<JsonObject>> mapMetricValuesToResources(JsonArray resources) {
        //noinspection unchecked
        return Observable
            .fromIterable((List<JsonObject>) resources.getList())
            .flatMapSingle(this::findMetricValuesForResource)
            .toList();
    }

    protected Single<JsonObject> findMetricValuesForResource(JsonObject jsonResource) {
        //noinspection unchecked
        return Observable
            .fromIterable((List<JsonObject>) jsonResource
                .getJsonArray("metric_values")
                .getList())
            .flatMapSingle(metricValue -> metricValueChecker.checkFindOne(metricValue.getLong("metric_value_id")))
            .toList()
            .map(metrics -> {
                jsonResource.put("metric_values", metrics);
                return jsonResource;
            });
    }

    protected List<Resource> mapJsonListToResourceList(List<JsonObject> jsonObjectList) {
        List<Resource> resourceList = new ArrayList<>();
        for (JsonObject resource : jsonObjectList) {
            resourceList.add(resource.mapTo(Resource.class));
        }
        return resourceList;
    }

    protected JsonArray filterAndSortResultList(List<Resource> resources,
                                                List<ServiceLevelObjective> serviceLevelObjectives, int limit) {
        List<JsonObject> filteredAndSorted = resources
            .stream()
            .filter(resource -> resourceFilterBySLOValueType(resource, serviceLevelObjectives))
            .sorted((r1, r2) -> sortResourceBySLO(r1, r2, serviceLevelObjectives))
            .map(JsonObject::mapFrom)
            .collect(Collectors.toList());
        List<JsonObject> subList = filteredAndSorted.subList(0, Math.min(limit, filteredAndSorted.size()));

        return new JsonArray(subList);
    }

    protected boolean resourceFilterBySLOValueType(Resource resource, List<ServiceLevelObjective> serviceLevelObjectives) {
        for (ServiceLevelObjective slo : serviceLevelObjectives) {
            for (MetricValue metricValue : resource.getMetricValues()) {
                Metric metric = metricValue.getMetric();
                if (metric.getMetric().equals(slo.getName())) {
                    if (!SLOCompareUtility.compareMetricValueWithSLO(metricValue, slo)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected int sortResourceBySLO(Resource r1, Resource r2, List<ServiceLevelObjective> serviceLevelObjectives) {
        for (int i = 0; i < serviceLevelObjectives.size(); i++) {
            ServiceLevelObjective slo = serviceLevelObjectives.get(i);
            if (slo.getValue().get(0).getSloValueType() != SLOValueType.NUMBER) {
                continue;
            }
            for (MetricValue metricValue1 : r1.getMetricValues()) {
                Metric metric1 = metricValue1.getMetric();
                if (metric1.getMetric().equals(slo.getName())) {
                    for (MetricValue metricValue2 : r2.getMetricValues()) {
                        Metric metric2 = metricValue2.getMetric();
                        if (metric2.getMetric().equals(slo.getName())) {
                            int compareValue = ExpressionType.compareValues(slo.getExpression(),
                                metricValue1.getValueNumber().doubleValue(),
                                metricValue2.getValueNumber().doubleValue());
                            if (compareValue != 0 || i == serviceLevelObjectives.size() - 1) {
                                return compareValue;
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }
}
