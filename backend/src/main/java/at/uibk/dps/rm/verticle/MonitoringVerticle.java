package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.handler.monitoring.AWSPriceListMonitoringHandler;
import at.uibk.dps.rm.handler.monitoring.K8sMonitoringHandler;
import at.uibk.dps.rm.handler.monitoring.MonitoringHandler;
import at.uibk.dps.rm.handler.monitoring.RegionMonitoringHandler;
import at.uibk.dps.rm.service.ServiceProxyBinder;
import at.uibk.dps.rm.service.monitoring.function.FunctionExecutionService;
import at.uibk.dps.rm.service.monitoring.function.FunctionExecutionServiceImpl;
import at.uibk.dps.rm.service.monitoring.metricpusher.FunctionInvocationPushService;
import at.uibk.dps.rm.service.monitoring.metricpusher.FunctionInvocationPushServiceImpl;
import at.uibk.dps.rm.service.monitoring.metricpusher.RegionMetricPushService;
import at.uibk.dps.rm.service.monitoring.metricpusher.RegionMetricPushServiceImpl;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
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

    private WebClient webClient;

    private final Set<MonitoringHandler> monitoringHandlers = new HashSet<>();

    @Override
    public Completable rxStart() {
        webClient = WebClient.create(vertx);
        ConfigDTO config = config().mapTo(ConfigDTO.class);
        monitoringHandlers.add(new K8sMonitoringHandler(vertx, config));
        monitoringHandlers.add(new RegionMonitoringHandler(vertx, config));
        monitoringHandlers.add(new AWSPriceListMonitoringHandler(vertx, webClient, config));
        return setupEventBus(config)
            .andThen(startMonitoringLoops());
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

            serviceProxyBinder.bind(FunctionExecutionService.class, new FunctionExecutionServiceImpl(webClient));
            serviceProxyBinder.bind(FunctionInvocationPushService.class,
                new FunctionInvocationPushServiceImpl(webClient, config));
            serviceProxyBinder.bind(RegionMetricPushService.class, new RegionMetricPushServiceImpl(webClient, config));
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupEventBus);
    }
}
