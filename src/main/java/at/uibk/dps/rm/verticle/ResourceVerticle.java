package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.resourcemanager.ResourceStore;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.resourcemanager.ResourceManagerService;
import at.uibk.dps.rm.service.resourcemanager.ResourceManagerServiceImpl;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

public class ResourceVerticle  extends AbstractVerticle {

    private ResourceStore resourceStore;

    @Override
    public Completable rxStart() {
        ServiceProxyProvider serviceProxyProvider = new ServiceProxyProvider(vertx);
        resourceStore = new ResourceStore(serviceProxyProvider);
        return resourceStore.initResources()
                .andThen(setupEventBus());
    }

    private Completable setupEventBus() {
        Maybe<Void> setupEventBus = Maybe.create(emitter -> {
            ResourceManagerService resourceManagerService = new ResourceManagerServiceImpl(resourceStore);
            new ServiceBinder(vertx.getDelegate())
                    .setAddress("resource-manager-service-address")
                    .register(ResourceManagerService.class, resourceManagerService);
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupEventBus);
    }
}
