package at.uibk.dps.rm.resourcemanager;

import at.uibk.dps.rm.handler.metric.MetricValueChecker;
import at.uibk.dps.rm.repository.resource.entity.Resource;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ResourceStore {

    private final HashMap<Long, Resource> resources = new HashMap<>();

    private final HashSet<Long> reservedResources = new HashSet<>();

    private final ServiceProxyProvider serviceProxyProvider;

    public ResourceStore(ServiceProxyProvider serviceProxyProvider) {
        this.serviceProxyProvider = serviceProxyProvider;
    }

    public Completable initResources() {
        //noinspection unchecked
        return retrieveResourcesFromDB()
                .flatMap(resources -> Observable
                        .fromIterable((List<JsonObject>) resources.getList())
                        .flatMapSingle(this::findMetricValuesForResource)
                        .toList())
                .map(resourcesJson -> {
                    loadResourcesIntoMemory(resourcesJson);
                    return resourcesJson;
                }).ignoreElement();
    }

    private Single<JsonArray> retrieveResourcesFromDB() {
        return serviceProxyProvider.getResourceService()
                .findAll();
    }

    private void loadResourcesIntoMemory(List<JsonObject> resourceList) {
        for (JsonObject entries : resourceList) {
            Resource resource = entries.mapTo(Resource.class);
            resources.put(resource.getResourceId(), resource);
        }
    }

    public Single<JsonArray> getAllResources() {
        return Observable.fromStream(this.resources.values().stream())
                .map(JsonObject::mapFrom)
                .toList()
                .map(JsonArray::new);
    }

    private Single<JsonObject> findMetricValuesForResource(JsonObject jsonResource) {
        MetricValueChecker metricValueChecker = new MetricValueChecker(serviceProxyProvider.getMetricValueService());
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
