package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.service.ServiceProxyBinder;
import at.uibk.dps.rm.service.deployment.DeploymentExecutionServiceImpl;
import at.uibk.dps.rm.service.deployment.DeploymentExecutionService;
import at.uibk.dps.rm.service.monitoring.function.FunctionExecutionService;
import at.uibk.dps.rm.service.monitoring.function.FunctionExecutionServiceImpl;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.ext.web.client.WebClient;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * Everything that relates to the deployment is executed on the DeploymentVerticle.
 *
 * @author matthi-g
 */
public class DeploymentVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentVerticle.class);

    private WebClient webClient;

    @Override
    public Completable rxStart() {
        webClient = WebClient.create(vertx);
        return setupEventBus();
    }

    /**
     * Register all deployment service proxies on the event bus.
     *
     * @return a Completable
     */
    private Completable setupEventBus() {
        Maybe<Void> setupEventBus = Maybe.create(emitter -> {
            ServiceBinder serviceBinder = new ServiceBinder(vertx.getDelegate());
            ServiceProxyBinder serviceProxyBinder = new ServiceProxyBinder(serviceBinder);
            DeploymentExecutionService deploymentService =
                new DeploymentExecutionServiceImpl();

            serviceBinder
                .setAddress("deployment-execution-service-address")
                .register(DeploymentExecutionService.class, deploymentService);
            serviceProxyBinder.bind(FunctionExecutionService.class, new FunctionExecutionServiceImpl(webClient));
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupEventBus)
            .doOnComplete(() -> logger.info("Deployment Verticle is ready"))
            .doOnError(throwable -> logger.error("Error", throwable));
    }
}
