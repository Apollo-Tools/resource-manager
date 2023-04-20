package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
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

/**
 * Processes the http requests that concern the metric_value entity.
 *
 * @author matthi-g
 */
public class MetricValueHandler extends ValidationHandler {

    private final MetricValueChecker metricValueChecker;

    private final MetricChecker metricChecker;

    private final ResourceChecker resourceChecker;

    /**
     * Create an instance from the metricValueChecker, metricChecker and resourceChecker.
     *
     * @param metricValueChecker the metric value checker
     * @param metricChecker the metric checker
     * @param resourceChecker the resource checker
     */
    public MetricValueHandler(MetricValueChecker metricValueChecker, MetricChecker metricChecker,
                              ResourceChecker resourceChecker) {
        super(metricValueChecker);
        this.metricValueChecker = metricValueChecker;
        this.metricChecker = metricChecker;
        this.resourceChecker = resourceChecker;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> resourceChecker.checkExistsOne(id)
                .andThen(Single.just(id)))
            .flatMap(id -> metricValueChecker.checkFindAllByResource(id, true));
    }

    @Override
    public Completable postAll(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> resourceChecker.checkExistsOne(id)
                .andThen(Single.just(id)))
            .flatMapCompletable(id -> checkAddMetricsResourceExists(requestBody, id)
                .flatMapCompletable(metricValues -> {
                    JsonArray metricValuesJson = Json.encodeToBuffer(metricValues)
                        .toJsonArray();
                    metricValuesJson.forEach(entry -> {
                            JsonObject metricValue = (JsonObject) entry;
                            JsonObject resource = new JsonObject();
                            resource.put("resource_id", id);
                            metricValue.put("resource", resource);
                        });
                    return metricValueChecker
                            .submitCreateAll(metricValuesJson);
                    }));
    }

    @Override
    public Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "resourceId")
            .flatMap(resourceId -> HttpHelper.getLongPathParam(rc, "metricId")
            .map(metricId -> Map.of("resourceId", resourceId, "metricId", metricId))
            .flatMap(ids -> checkMetricValueExists(ids.get("resourceId"), ids.get("metricId"))
                .andThen(metricChecker.checkUpdateMetricValueSetCorrectly(requestBody,  ids.get("metricId")))
                .andThen(Single.just(ids))
            ))
            .map(result -> result)
            .flatMapCompletable(ids -> updateOneByValue(requestBody, ids));
    }

    @Override
    public Completable deleteOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "resourceId")
            .flatMap(resourceId -> HttpHelper.getLongPathParam(rc, "metricId")
                .map(metricId -> Map.of("resourceId", resourceId, "metricId", metricId))
                .flatMap(ids -> checkMetricValueExists(ids.get("resourceId"), ids.get("metricId"))
                .andThen(Single.just(ids))
            ))
            .flatMapCompletable(ids -> metricValueChecker.submitDeleteMetricValue(ids.get("resourceId"), ids.get("metricId")));
    }

    /**
     * Check if the metrics that should be added to the resource are already present and exist.
     *
     * @param requestBody the request body that contains the metrics
     * @param resourceId the id of the resource
     * @return a Single that emits a list of metric values
     */
    private Single<List<MetricValue>> checkAddMetricsResourceExists(JsonArray requestBody, long resourceId) {
        List<MetricValue> metricValues = new ArrayList<>();
        List<Completable> completables = checkAddMetricList(requestBody, resourceId, metricValues);
        return Completable.merge(completables)
            .andThen(Single.just(metricValues));
    }

    /**
     * Check if adding metrics to a resource would cause any violations.
     *
     * @param requestBody the request body that contains the metrics to add
     * @param resourceId the id of the resource
     * @param metricValues the list where the new metric values are stored.
     * @return a List of completables
     */
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
            completables.add(metricChecker.checkAddMetricValueSetCorrectly(jsonMetric, metricId, metricValue));
            completables.add(metricValueChecker.checkForDuplicateByResourceAndMetric(resourceId, metricId));
        });
        return completables;
    }

    /**
     * Check if a metric value exists by its resourceId and metricId.
     *
     * @param resourceId the id of the resource
     * @param metricId  the id of the metric
     * @return a Completable
     */
    private Completable checkMetricValueExists(long resourceId, long metricId) {
        return Completable.mergeArray(
            resourceChecker.checkExistsOne(resourceId),
            metricChecker.checkExistsOne(metricId),
            metricValueChecker.checkMetricValueExistsByResourceAndMetric(resourceId, metricId));
    }

    /**
     * Update a metric value depending on its type.
     *
     * @param requestBody the request body
     * @param ids contains the id of the resource and id of the metric
     * @return a Completable
     */
    private Completable updateOneByValue(JsonObject requestBody, Map<String, Long> ids) {
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
    }
}
