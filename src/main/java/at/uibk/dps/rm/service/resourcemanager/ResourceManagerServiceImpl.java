package at.uibk.dps.rm.service.resourcemanager;

import at.uibk.dps.rm.resourcemanager.ResourceStore;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.SingleHelper;

public class ResourceManagerServiceImpl implements ResourceManagerService {

    private final ResourceStore resourceStore;

    public ResourceManagerServiceImpl(ResourceStore resourceStore) {
        this.resourceStore = resourceStore;
    }

    public Future<JsonArray> getAll() {
        return SingleHelper.toFuture(resourceStore.getAllResources());
    }
}
