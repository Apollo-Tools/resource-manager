package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.*;
import at.uibk.dps.rm.handler.metric.MetricChecker;
import at.uibk.dps.rm.handler.metric.MetricValueChecker;
import at.uibk.dps.rm.repository.metric.entity.MetricValue;
import at.uibk.dps.rm.service.rxjava3.metric.MetricValueService;
import at.uibk.dps.rm.service.rxjava3.metric.MetricService;
import at.uibk.dps.rm.service.rxjava3.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.resource.ResourceTypeService;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;

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

    protected Single<JsonArray> getResourceBySLOs(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        JsonArray serviceLevelObjectives = requestBody.getJsonArray("slo");
        List<Completable> completables = new ArrayList<>();
        serviceLevelObjectives.stream().forEach(entity -> {
            JsonObject metric = (JsonObject) entity;
            completables.add(metricChecker.checkExistsOne(metric.getString("metric")));
        });
        return Completable.merge(completables)
            .andThen(Observable.fromStream(serviceLevelObjectives.stream())
                .map(item -> ((JsonObject) item).getString("metric"))
                .toList())
            .flatMap(metricValueChecker::checkFindAllByMultipleMetrics);
    }


    private Single<JsonObject> checkUpdateResourceTypeExists(JsonObject requestBody, JsonObject entity) {
        if (requestBody.containsKey("resource_type")) {
            return resourceTypeChecker.checkExistsOne(requestBody.getJsonObject("resource_type").getLong("type_id"))
                .andThen(Single.just(entity));
        }
        return Single.just(entity);
    }
}
