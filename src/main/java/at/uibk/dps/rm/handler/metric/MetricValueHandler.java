package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricValueService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MetricValueHandler extends ValidationHandler {

    private final MetricValueChecker metricValueChecker;

    private final MetricChecker metricChecker;

    private final ResourceChecker resourceChecker;

    public MetricValueHandler(MetricValueService metricValueService,
                              MetricService metricService, ResourceService resourceService) {
        super(new MetricValueChecker(metricValueService));
        metricValueChecker = (MetricValueChecker) super.entityChecker;
        metricChecker = new MetricChecker(metricService);
        resourceChecker = new ResourceChecker(resourceService);
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> resourceChecker.checkExistsOne(id)
                .andThen(Single.just(id)))
            .flatMap(metricValueChecker::checkFindAllByResource);
    }

    @Override
    public Completable postAll(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> resourceChecker.checkExistsOne(id)
                .andThen(Single.just(id)))
            .flatMap(id -> checkAddMetricsResourceExists(requestBody, id))
            .flatMapCompletable(metricValues -> metricValueChecker.submitCreateAll(Json.encodeToBuffer(metricValues).toJsonArray()));
    }

    @Override
    public Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "resourceId")
                .flatMap(resourceId -> HttpHelper.getLongPathParam(rc, "metricId")
                .map(metricId -> Map.of("resourceId", resourceId, "metricId", metricId))
                .flatMap(ids -> checkUpdateDeleteMetricValueExists(ids.get("resourceId"), ids.get("metricId"))
                    .andThen(checkUpdateMetricValueSetCorrectly(requestBody,  ids.get("metricId")))
                    .andThen(Single.just(ids))
                ))
                .flatMapCompletable(ids -> {
                    Object value = requestBody.getValue("value");
                    long resourceId = ids.get("resourceId");
                    long metricId = ids.get("metricId");
                    if (value instanceof String) {
                        return metricValueChecker.submitUpdateMetricValue(resourceId, metricId, (String) value);
                    } else if (value instanceof Number) {
                        return metricValueChecker.submitUpdateMetricValue(resourceId, metricId, ((Number) value).doubleValue());
                    } else if (value instanceof Boolean) {
                        return metricValueChecker.submitUpdateMetricValue(resourceId, metricId, (Boolean) value);
                    }
                    return Completable.error(new BadInputException());
                });
    }

    @Override
    public Completable deleteOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "resourceId")
            .flatMap(resourceId -> HttpHelper.getLongPathParam(rc, "metricId")
                .map(metricId -> Map.of("resourceId", resourceId, "metricId", metricId))
                .flatMap(ids -> checkUpdateDeleteMetricValueExists(ids.get("resourceId"), ids.get("metricId"))
                .andThen(Single.just(ids))
            ))
            .flatMapCompletable(ids -> metricValueChecker.submitDeleteMetricValue(ids.get("resourceId"), ids.get("metricId")));
    }

    private Single<List<MetricValue>> checkAddMetricsResourceExists(JsonArray requestBody, long resourceId) {
        List<MetricValue> metricValues = new ArrayList<>();
        List<Completable> completables = checkAddMetricList(requestBody, resourceId, metricValues);
        return Completable.merge(completables)
            .andThen(Single.just(metricValues));
    }

    private List<Completable> checkAddMetricList(JsonArray requestBody, long resourceId, List<MetricValue> metricValues) {
        Resource resource = new Resource();
        resource.setResourceId(resourceId);
        List<Completable> completables = new ArrayList<>();
        requestBody.stream().forEach(jsonObject -> {
            JsonObject jsonMetric = (JsonObject) jsonObject;
            long metricId = jsonMetric.getLong("metricId");
            Metric metric = new Metric();
            metric.setMetricId(metricId);
            MetricValue metricValue = new MetricValue();
            metricValue.setResource(resource);
            metricValue.setMetric(metric);
            metricValues.add(metricValue);
            completables.add(checkAddMetricValueSetCorrectly(jsonMetric, metricId, metricValue));
            completables.add(metricChecker.checkExistsOne(metricId));
            completables.add(metricValueChecker.checkForDuplicateByResourceAndMetric(resourceId, metricId));
        });
        return completables;
    }

    private Completable checkAddMetricValueSetCorrectly(JsonObject jsonEntry, long metricId, MetricValue metricValue) {
        Single<Boolean> checkMetricValue = metricChecker.checkFindOne(metricId)
                .map(result -> {
                    Metric metric = result.mapTo(Metric.class);
                    Object value = jsonEntry.getValue("value");
                    boolean valueHasCorrectType = true;
                    if (stringMetricHasStringValue(metric, value)) {
                        metricValue.setValueString((String) value);
                    } else if (numberMetricHasStringValue(metric, value)) {
                        metricValue.setValueNumber(jsonEntry.getNumber("value").doubleValue());
                    } else if (boolMetricHasStringValue(metric, value)) {
                        metricValue.setValueBool((Boolean) value);
                    } else {
                        valueHasCorrectType = false;
                    }
                    return valueHasCorrectType;
                });
        return ErrorHandler.handleBadInput(checkMetricValue).ignoreElement();
    }

    private Completable checkUpdateMetricValueSetCorrectly(JsonObject jsonEntry, long metricId) {
        Single<Boolean> checkMetricValue = metricChecker.checkFindOne(metricId)
                .map(result -> {
                    Metric metric = result.mapTo(Metric.class);
                    Object value = jsonEntry.getValue("value");
                    return hasNoMetricValue(metric, value) || stringMetricHasStringValue(metric, value) ||
                            numberMetricHasStringValue(metric, value) || boolMetricHasStringValue(metric, value);
                });
        return ErrorHandler.handleBadInput(checkMetricValue).ignoreElement();
    }

    private boolean hasNoMetricValue(Metric metric, Object value) {
        return metric.getMonitored() && value == null;
    }

    private boolean stringMetricHasStringValue(Metric metric, Object value) {
        return value instanceof String && metric.getMetricType().getType().equals("string");
    }

    private boolean numberMetricHasStringValue(Metric metric, Object value) {
        return value instanceof Number && metric.getMetricType().getType().equals("number");
    }

    private boolean boolMetricHasStringValue(Metric metric, Object value) {
        return value instanceof Boolean && metric.getMetricType().getType().equals("bool");
    }

    private Completable checkUpdateDeleteMetricValueExists(long resourceId, long metricId) {
        return Completable.mergeArray(
            resourceChecker.checkExistsOne(resourceId),
            metricChecker.checkExistsOne(metricId),
            metricValueChecker.checkMetricValueExistsByResourceAndMetric(resourceId, metricId));
    }
}
