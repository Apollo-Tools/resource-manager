package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.dto.GetResourcesBySLOsRequest;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.handler.*;
import at.uibk.dps.rm.handler.metric.MetricChecker;
import at.uibk.dps.rm.handler.metric.MetricValueChecker;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricValueService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceTypeService;
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

public class ResourceHandler extends ValidationHandler {

    private final ResourceTypeChecker resourceTypeChecker;

    private final MetricChecker metricChecker;

    private final MetricValueChecker metricValueChecker;

    public ResourceHandler(ResourceService resourceService, ResourceTypeService resourceTypeService,
                           MetricService metricService, MetricValueService metricValueService) {
        super(new ResourceChecker(resourceService));
        resourceTypeChecker = new ResourceTypeChecker(resourceTypeService);
        metricChecker = new MetricChecker(metricService);
        metricValueChecker = new MetricValueChecker(metricValueService);
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return resourceTypeChecker.checkExistsOne(requestBody
                .getJsonObject("resource_type")
                .getLong("type_id"))
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMap(result -> entityChecker.submitCreate(requestBody));
    }

    @Override
    public Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(entityChecker::checkFindOne)
            .flatMap(result -> checkUpdateResourceTypeExists(requestBody, result))
            .flatMapCompletable(result -> entityChecker.submitUpdate(requestBody, result));
    }

    //TODO: simplify
    // remove reserved resources
    protected Single<JsonArray> getResourceBySLOs(RoutingContext rc) {
        ResourceChecker resourceChecker = (ResourceChecker) super.entityChecker;
        GetResourcesBySLOsRequest requestDTO = rc.body()
                .asJsonObject()
                .mapTo(GetResourcesBySLOsRequest.class);
        List<ServiceLevelObjective> serviceLevelObjectives = requestDTO.getServiceLevelObjectives();
        List<Completable> completables = new ArrayList<>();
        requestDTO.getServiceLevelObjectives().forEach(slo ->
                completables.add(metricChecker.checkFindOneByMetric(slo.getName())
                        .flatMap(metric -> metricChecker.checkEqualValueTypes(slo, metric))
                        .ignoreElement()));
        //noinspection unchecked
        return Completable.merge(completables)
                .andThen(Observable.fromStream(serviceLevelObjectives.stream())
                        .map(ServiceLevelObjective::getName)
                        .toList())
                .flatMap(resourceChecker::checkFindAllByMultipleMetrics)
                .flatMap(resources -> Observable
                        .fromIterable((List<JsonObject>) resources.getList())
                        .flatMapSingle(this::findMetricValuesForResource)
                        .toList())
                .map(resources -> {
                    List<Resource> resourceList = new ArrayList<>();
                    for (JsonObject resource : resources) {
                        resourceList.add(resource.mapTo(Resource.class));
                    }
                    return resourceList;
                })
                .map(resources -> {
                    int limit = requestDTO.getLimit();
                    List<Resource> result = resources
                            .stream()
                            .filter(resource -> resourceFilterBySLO(resource, serviceLevelObjectives))
                            .sorted((r1, r2) -> sortResourceBySLO(r1, r2, serviceLevelObjectives))
                            .collect(Collectors.toList());
                    return result.subList(0, Math.min(limit, result.size()));
                })
                .map(JsonArray::new);
    }


    private Single<JsonObject> checkUpdateResourceTypeExists(JsonObject requestBody, JsonObject entity) {
        if (requestBody.containsKey("resource_type")) {
            return resourceTypeChecker.checkExistsOne(requestBody.getJsonObject("resource_type").getLong("type_id"))
                .andThen(Single.just(entity));
        }
        return Single.just(entity);
    }

    private Single<JsonObject> findMetricValuesForResource(JsonObject jsonResource) {
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

    protected boolean resourceFilterBySLO(Resource resource, List<ServiceLevelObjective> serviceLevelObjectives) {
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
