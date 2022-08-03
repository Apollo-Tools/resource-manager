package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.repository.metric.entity.Metric;
import at.uibk.dps.rm.repository.metric.entity.MetricValue;
import at.uibk.dps.rm.repository.resource.entity.Resource;
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
    public Completable deleteOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "resourceId")
            .flatMap(resourceId -> HttpHelper.getLongPathParam(rc, "metricId")
                .map(metricId -> Map.of("resourceId", resourceId, "metricId", metricId))
                .flatMap(ids -> checkDeleteMetricValueExists(ids.get("resourceId"), ids.get("metricId"))
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
            completables.add(metricChecker.checkExistsOne(metricId));
            completables.add(metricValueChecker.checkForDuplicateByResourceAndMetric(resourceId, metricId));
        });
        return completables;
    }

    private Completable checkDeleteMetricValueExists(long resourceId, long metricId) {
        return Completable.mergeArray(
            resourceChecker.checkExistsOne(resourceId),
            metricChecker.checkExistsOne(metricId),
            metricValueChecker.checkMetricValueExistsByResourceAndMetric(resourceId, metricId));
    }
}
