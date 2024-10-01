package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.handler.ensemble.EnsembleHandler;
import at.uibk.dps.rm.handler.monitoring.*;
import at.uibk.dps.rm.service.ServiceProxyBinder;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.monitoring.aws.EC2PriceMonitoring;
import at.uibk.dps.rm.service.monitoring.aws.LambdaPriceMonitoring;
import at.uibk.dps.rm.service.monitoring.k8s.K8sMonitoringServiceImpl;
import at.uibk.dps.rm.service.monitoring.metricquery.MetricQueryService;
import at.uibk.dps.rm.service.monitoring.metricquery.MetricQueryServiceImpl;
import at.uibk.dps.rm.service.monitoring.metricpusher.*;
import at.uibk.dps.rm.util.monitoring.ComputePriceUtility;
import at.uibk.dps.rm.util.monitoring.LatencyMonitoringUtility;
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
        ServiceProxyProvider serviceProxyProvider = new ServiceProxyProvider(vertx);
        ConfigDTO config = config().mapTo(ConfigDTO.class);
        LambdaPriceMonitoring lambdaMonitoring = new LambdaPriceMonitoring(webClient, new ComputePriceUtility());
        EC2PriceMonitoring ec2PriceMonitoring = new EC2PriceMonitoring(webClient, new ComputePriceUtility());
        LatencyMonitoringUtility latencyMonitoringUtility = new LatencyMonitoringUtility();
        monitoringHandlers.add(new AWSPriceListMonitoringHandler(vertx, config, serviceProxyProvider,
            lambdaMonitoring, ec2PriceMonitoring));
        EnsembleHandler ensembleHandler = new EnsembleHandler(serviceProxyProvider.getEnsembleService(),
            serviceProxyProvider.getResourceService(), serviceProxyProvider.getMetricService(),
            serviceProxyProvider.getMetricQueryService());
        monitoringHandlers.add(new EnsembleValidationHandler(vertx, config, ensembleHandler));
        monitoringHandlers.add(new K8sMonitoringHandler(vertx, config, serviceProxyProvider,
            new K8sMonitoringServiceImpl(), latencyMonitoringUtility));
        monitoringHandlers.add(new OpenFaasMonitoringHandler(vertx, config, serviceProxyProvider,
            latencyMonitoringUtility));
        monitoringHandlers.add(new RegionMonitoringHandler(vertx, config, serviceProxyProvider,
            latencyMonitoringUtility));
        monitoringHandlers.add(new FileCleanupHandler(vertx, config, serviceProxyProvider));
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
            serviceProxyBinder.bind(ServiceStartupShutdownPushService.class,
                new ServiceStartupShutdownPushServiceImpl(webClient, config));
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
