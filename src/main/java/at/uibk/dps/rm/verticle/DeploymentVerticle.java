package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.service.deployment.DeploymentServiceImpl;
import at.uibk.dps.rm.service.deployment.DeploymentService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

public class DeploymentVerticle extends AbstractVerticle {

    @Override
    public Completable rxStart() {
        return setupEventBus();
    }

    private Completable setupEventBus() {
        Maybe<Void> setupEventBus = Maybe.create(emitter -> {
            DeploymentService deploymentService =
                new DeploymentServiceImpl();
            new ServiceBinder(vertx.getDelegate())
                .setAddress("deployment-service-address")
                .register(DeploymentService.class, deploymentService);
        });
        return setupEventBus.ignoreElement();
    }
}
