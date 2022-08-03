package at.uibk.dps.rm.resourcemanager;

import at.uibk.dps.rm.repository.resource.entity.Resource;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;

public class ResourceStore {

    private final HashMap<Long, Resource> resources = new HashMap<>();

    private final ServiceProxyProvider serviceProxyProvider;

    public ResourceStore(ServiceProxyProvider serviceProxyProvider) {
        this.serviceProxyProvider = serviceProxyProvider;
    }

    public Completable initResources() {
        return retrieveResourcesFromDB()
                .map(resourcesJson -> {
                    loadResourcesIntoMemory(resourcesJson);
                    return resourcesJson;
                }).ignoreElement();
    }

    private Single<JsonArray> retrieveResourcesFromDB() {
        return serviceProxyProvider.getResourceService()
                .findAll();
    }

    private void loadResourcesIntoMemory(JsonArray resourceList) {
        for (int i = 0; i < resourceList.size(); i ++) {
            Resource resource = resourceList.getJsonObject(i).mapTo(Resource.class);
            resources.put(resource.getResourceId(), resource);
        }
    }

    public Single<JsonArray> getAllResources() {
        return Observable.fromStream(this.resources.values().stream())
                .map(JsonObject::mapFrom)
                .toList()
                .map(JsonArray::new);
    }
}
