package at.uibk.dps.rm.handler.Resource;

import at.uibk.dps.rm.handler.*;
import at.uibk.dps.rm.repository.metric.entity.Metric;
import at.uibk.dps.rm.repository.metric.entity.MetricValue;
import at.uibk.dps.rm.repository.resource.entity.Resource;
import at.uibk.dps.rm.service.rxjava3.metric.MetricService;
import at.uibk.dps.rm.service.rxjava3.metric.MetricValueService;
import at.uibk.dps.rm.service.rxjava3.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.resource.ResourceTypeService;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResourceHandler extends RequestHandler {
    private final ResourceService resourceService;

    private final ResourceTypeService resourceTypeService;

    private final MetricService metricService;

    private final MetricValueService metricValueService;

    public ResourceHandler(Vertx vertx) {
        super(ResourceService.createProxy(vertx,"resource-service-address"));
        resourceService = (ResourceService) super.service;
        resourceTypeService = ResourceTypeService.createProxy(vertx, "resource-type-service-address");
        metricService = MetricService.createProxy(vertx, "metric-service-address");
        metricValueService = MetricValueService.createProxy(vertx, "metric-value-service-address");
    }

    @Override
    public Disposable post(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return checkForDuplicateResource(requestBody.getString("url"))
            .andThen(checkResourceTypeExists(requestBody.getJsonObject("resource_type").getLong("type_id")))
            .andThen(submitCreate(requestBody))
            .subscribe(result -> ResultHandler.handleSaveRequest(rc, result),
                throwable -> ErrorHandler.handleRequestError(rc, throwable));
    }

    public Disposable postMetrics(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> checkExistsOne(id)
                .andThen(Single.just(id)))
            .flatMap(id -> checkAddMetricsResourceExists(requestBody, id))
            .flatMapCompletable(this::submitAddMetrics)
            .subscribe(() -> ResultHandler.handleSaveAllUpdateDeleteRequest(rc),
                throwable -> ErrorHandler.handleRequestError(rc, throwable));
    }

    public Disposable getMetrics(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> checkExistsOne(id)
                .andThen(Single.just(id)))
            .flatMap(this::submitFindMetrics)
            .subscribe(result -> ResultHandler.handleGetAllRequest(rc, result),
                throwable -> ErrorHandler.handleRequestError(rc, throwable));
    }

    @Override
    public Disposable patch(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(super::checkFindOne)
            .flatMap(result -> checkUpdateNoDuplicate(requestBody, result))
            .flatMap(result -> checkUpdateResourceTypeExists(requestBody, result))
            .flatMapCompletable(result -> submitUpdate(requestBody, result))
            .subscribe(() -> ResultHandler.handleSaveAllUpdateDeleteRequest(rc),
                throwable -> ErrorHandler.handleRequestError(rc, throwable));
    }

    public Disposable deleteMetric(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "resourceId")
            .flatMap(resourceId -> HttpHelper.getLongPathParam(rc, "metricId")
                .map(metricId -> Map.of("resourceId", resourceId, "metricId", metricId))
                .flatMap(ids -> checkDeleteMetricValueExists(ids.get("resourceId"), ids.get("metricId"))
                    .andThen(Single.just(ids))
                ))
            .flatMapCompletable(ids -> submitDeleteMetricValue(ids.get("resourceId"), ids.get("metricId")))
            .subscribe(() -> ResultHandler.handleSaveAllUpdateDeleteRequest(rc),
                throwable -> ErrorHandler.handleRequestError(rc, throwable));
    }

    private Completable submitAddMetrics(List<MetricValue> metricValues) {
        return metricValueService.saveAll(Json.encodeToBuffer(metricValues).toJsonArray());
    }

    private Single<JsonArray> submitFindMetrics(long id) {
        return metricValueService.findAllByResource(id);
    }

    private Completable submitDeleteMetricValue(long resourceId, long metricId) {
        return metricValueService.deleteByResourceAndMetric(resourceId, metricId);
    }

    private Completable checkForDuplicateResource(String url) {
        Single<Boolean> existsOneByUrl = resourceService.existsOneByUrl(url);
        return ErrorHandler.handleDuplicates(existsOneByUrl).ignoreElement();
    }

    private Completable checkForDuplicateMetricValue(long resourceId, long metricId) {
        Single<Boolean> existsOneByResourceAndMetric = metricValueService.existsOneByResourceAndMetric(resourceId, metricId);
        return ErrorHandler.handleDuplicates(existsOneByResourceAndMetric).ignoreElement();
    }

    protected Completable checkResourceTypeExists(long id) {
        Single<Boolean> existsResourceTypeById = resourceTypeService.existsOneById(id);
        return ErrorHandler.handleExistsOne(existsResourceTypeById).ignoreElement();
    }

    private Completable checkMetricExists(long id) {
        Single<Boolean> existsOneById = metricService.existsOneById(id);
        return ErrorHandler.handleExistsOne(existsOneById).ignoreElement();
    }

    private Completable checkMetricValueExists(long resourceId, long metricId) {
        Single<Boolean> existsOneByResourceAndMetric =
            metricValueService.existsOneByResourceAndMetric(resourceId, metricId);
        return ErrorHandler.handleExistsOne(existsOneByResourceAndMetric).ignoreElement();
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
            completables.add(checkMetricExists(metricId));
            completables.add(checkForDuplicateMetricValue(resourceId, metricId));
        });
        return completables;
    }

    private Single<JsonObject> checkUpdateNoDuplicate(JsonObject requestBody, JsonObject entity) {
        if (requestBody.containsKey("url")) {
            return checkForDuplicateResource(requestBody.getString("url"))
                .andThen(Single.just(entity));
        }
        return Single.just(entity);
    }


    private Single<JsonObject> checkUpdateResourceTypeExists(JsonObject requestBody, JsonObject entity) {
        if (requestBody.containsKey("resource_type")) {
            return checkResourceTypeExists(requestBody.getJsonObject("resource_type").getLong("type_id"))
                .andThen(Single.just(entity));
        }
        return Single.just(entity);
    }

    private Completable checkDeleteMetricValueExists(long resourceId, long metricId) {
        return Completable.mergeArray(
            checkExistsOne(resourceId),
            checkMetricExists(metricId),
            checkMetricValueExists(resourceId, metricId));
    }
}
