package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.handler.monitoring.*;
import at.uibk.dps.rm.service.ServiceProxyBinder;
import at.uibk.dps.rm.service.monitoring.function.FunctionExecutionService;
import at.uibk.dps.rm.service.monitoring.function.FunctionExecutionServiceImpl;
import at.uibk.dps.rm.service.monitoring.metricquery.MetricQueryService;
import at.uibk.dps.rm.service.monitoring.metricquery.MetricQueryServiceImpl;
import at.uibk.dps.rm.service.monitoring.metricpusher.*;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.ext.web.client.WebClient;
import io.vertx.serviceproxy.ServiceBinder;

import java.util.*;

/**
 * All monitoring processes of the resource manager are executed on the ApiVerticle.
 *
 * @author matthi-g
 */
public class MonitoringVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringVerticle.class);

    private WebClient webClient;

    private final Set<MonitoringHandler> monitoringHandlers = new HashSet<>();

    @Override
    public Completable rxStart() {
        webClient = WebClient.create(vertx);
        ConfigDTO config = config().mapTo(ConfigDTO.class);
        monitoringHandlers.add(new AWSPriceListMonitoringHandler(vertx, webClient, config));
        monitoringHandlers.add(new K8sMonitoringHandler(vertx, config));
        monitoringHandlers.add(new OpenFaasMonitoringHandler(vertx, config));
        monitoringHandlers.add(new RegionMonitoringHandler(vertx, config));
        return setupEventBus(config)
            .andThen(startMonitoringLoops())
            .doOnComplete(() -> logger.info("Started monitoring loop"))
            .doOnError(throwable -> logger.error("Error", throwable));
    }

    private Completable startMonitoringLoops() {
        return Observable.fromIterable(monitoringHandlers)
            .flatMapCompletable(handler -> Completable.fromAction(handler::startMonitoringLoop));
    }

    /**
     * Register all monitoring service proxies on the event bus.
     *
     * @return a Completable
     */
    private Completable setupEventBus(ConfigDTO config) {
        Maybe<Void> setupEventBus = Maybe.create(emitter -> {
            ServiceBinder serviceBinder = new ServiceBinder(vertx.getDelegate());
            ServiceProxyBinder serviceProxyBinder = new ServiceProxyBinder(serviceBinder);

            serviceProxyBinder.bind(AWSPricePushService.class, new AWSPricePushServiceImpl(webClient, config));
            serviceProxyBinder.bind(ContainerStartupTerminationPushService.class,
                new ContainerStartupTerminationPushServiceImpl(webClient, config));
            serviceProxyBinder.bind(FunctionExecutionService.class, new FunctionExecutionServiceImpl(webClient));
            serviceProxyBinder.bind(FunctionInvocationPushService.class,
                new FunctionInvocationPushServiceImpl(webClient, config));
            serviceProxyBinder.bind(K8sMetricPushService.class, new K8sMetricPushServiceImpl(webClient, config));
            serviceProxyBinder.bind(MetricQueryService.class, new MetricQueryServiceImpl(webClient, config));
            serviceProxyBinder.bind(OpenFaasMetricPushService.class,
                new OpenFaasMetricPushServiceImpl(webClient, config));
            serviceProxyBinder.bind(RegionMetricPushService.class, new RegionMetricPushServiceImpl(webClient, config));
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupEventBus);
    }
}
