package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.*;
import at.uibk.dps.rm.handler.metric.MetricChecker;
import at.uibk.dps.rm.handler.metric.MetricValueChecker;
import at.uibk.dps.rm.repository.metric.entity.Metric;
import at.uibk.dps.rm.repository.metric.entity.MetricValue;
import at.uibk.dps.rm.repository.resource.entity.Resource;
import at.uibk.dps.rm.service.rxjava3.resourcemanager.ResourceManagerService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricValueService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceTypeService;
import at.uibk.dps.rm.slo.EvaluationType;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ResourceHandler extends ValidationHandler {

    private final ResourceTypeChecker resourceTypeChecker;

    private final MetricChecker metricChecker;

    private final MetricValueChecker metricValueChecker;

    private final ResourceManagerService resourceManagerService;

    public ResourceHandler(ResourceService resourceService, ResourceTypeService resourceTypeService,
                           MetricService metricService, MetricValueService metricValueService, ResourceManagerService resourceManagerService) {
        super(new ResourceChecker(resourceService));
        resourceTypeChecker = new ResourceTypeChecker(resourceTypeService);
        metricChecker = new MetricChecker(metricService);
        metricValueChecker = new MetricValueChecker(metricValueService);
        this.resourceManagerService = resourceManagerService;
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return entityChecker.checkForDuplicateEntity(requestBody)
            .andThen(resourceTypeChecker.checkExistsOne(requestBody
                                                            .getJsonObject("resource_type")
                                                            .getLong("type_id")))
            .andThen(entityChecker.submitCreate(requestBody));
    }

    @Override
    public Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(entityChecker::checkFindOne)
            .flatMap(result -> entityChecker.checkUpdateNoDuplicate(requestBody, result))
            .flatMap(result -> checkUpdateResourceTypeExists(requestBody, result))
            .flatMapCompletable(result -> entityChecker.submitUpdate(requestBody, result));
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return resourceManagerService.getAll();
    }

    protected Single<JsonArray> getResourceBySLOs(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        JsonArray serviceLevelObjectives = requestBody.getJsonArray("slo");
        List<Completable> completables = new ArrayList<>();
        serviceLevelObjectives.stream().forEach(entity -> {
            JsonObject metric = (JsonObject) entity;
            completables.add(metricChecker.checkExistsOne(metric.getString("metric")));
        });
        //noinspection unchecked
        return Completable.merge(completables)
                .andThen(Observable.fromStream(serviceLevelObjectives.stream())
                        .map(item -> ((JsonObject) item).getString("metric"))
                        .toList())
                .flatMap(metricValueChecker::checkFindAllByMultipleMetrics)
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
                    JsonArray sloArray = requestBody.getJsonArray("slo");
                    Integer limit = requestBody.getInteger("limit");
                    List<Resource> result = resources
                            .stream()
                            .filter(resource -> {
                                for (int i = 0; i < sloArray.size(); i++) {
                                    JsonObject slo = sloArray.getJsonObject(i);
                                    for (MetricValue metricValue : resource.getMetricValues()) {
                                        Metric metric = metricValue.getMetric();
                                        if (metric.getMetric().equals(slo.getString("metric"))) {
                                            // TODO: change json to snake case
                                            int compareValue = EvaluationType.compareValues(slo.getString("evaluationType"),
                                                    Double.parseDouble(slo.getString("metricExpression")),
                                                    metricValue.getValue().doubleValue());
                                            boolean isEqualityCheck = slo.getString("evaluationType")
                                                    .equals(EvaluationType.EQ.getSymbol());
                                            if (compareValue == 0 && !isEqualityCheck) {
                                                return false;
                                            } else if (compareValue != 0 && isEqualityCheck) {
                                                return false;
                                            } else if (compareValue == -1) {
                                                return false;
                                            }
                                        }
                                    }
                                }
                                return true;
                            })
                            .sorted((r1, r2) -> {
                                for (int i = 0; i < sloArray.size(); i++) {
                                    JsonObject slo = sloArray.getJsonObject(i);
                                    for (MetricValue metricValue1 : r1.getMetricValues()) {
                                        Metric metric1 = metricValue1.getMetric();
                                        if (metric1.getMetric().equals(slo.getString("metric"))) {
                                            for (MetricValue metricValue2 : r2.getMetricValues()) {
                                                Metric metric2 = metricValue2.getMetric();
                                                if (metric2.getMetric().equals(slo.getString("metric"))) {
                                                    // TODO: change json to snake case
                                                    int compareValue = EvaluationType.compareValues(slo.getString("evaluationType"),
                                                            metricValue1.getValue().doubleValue(),
                                                            metricValue2.getValue().doubleValue());
                                                    if (compareValue != 0 || i == sloArray.size() - 1) {
                                                        return compareValue;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                return 0;
                            })
                            .collect(Collectors.toList());
                    return result.subList(0, Math.min(Objects.requireNonNullElse(limit, 10000), result.size()));
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
}
