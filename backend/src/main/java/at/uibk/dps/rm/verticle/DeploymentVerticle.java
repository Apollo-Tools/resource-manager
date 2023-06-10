package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.service.deployment.DeploymentExecutionServiceImpl;
import at.uibk.dps.rm.service.deployment.DeploymentExecutionService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * Everything that relates to the deployment is executed on the DeploymentVerticle.
 *
 * @author matthi-g
 */
public class DeploymentVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentVerticle.class);

    @Override
    public Completable rxStart() {
        return setupEventBus();
    }

    /**
     * Register all deployment service proxies on the event bus.
     *
     * @return a Completable
     */
    private Completable setupEventBus() {
        Maybe<Void> setupEventBus = Maybe.create(emitter -> {
            DeploymentExecutionService deploymentService =
                new DeploymentExecutionServiceImpl();
            new ServiceBinder(vertx.getDelegate())
                .setAddress("deployment-execution-service-address")
                .register(DeploymentExecutionService.class, deploymentService);
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupEventBus);
    }
}
