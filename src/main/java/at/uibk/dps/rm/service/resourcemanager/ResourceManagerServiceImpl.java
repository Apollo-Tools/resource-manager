package at.uibk.dps.rm.service.resourcemanager;

import at.uibk.dps.rm.resourcemanager.ResourceStore;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.SingleHelper;

public class ResourceManagerServiceImpl implements ResourceManagerService {

    private final ResourceStore resourceStore;

    public ResourceManagerServiceImpl(ResourceStore resourceStore) {
        this.resourceStore = resourceStore;
    }

    public Future<JsonObject> getOne(long id) {
        JsonObject resource = resourceStore.getOneResource(id);
        if (resource == null) {
            return Future.fromCompletionStage(Completable.complete().toCompletionStage(null));
        }
        return SingleHelper.toFuture(Single.just(resourceStore.getOneResource(id)));
    }

    public Future<JsonArray> getAll() {
        return SingleHelper.toFuture(resourceStore.getAllResources());
    }
}
