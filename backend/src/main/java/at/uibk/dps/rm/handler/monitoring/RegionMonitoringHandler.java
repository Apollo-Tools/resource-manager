package at.uibk.dps.rm.handler.monitoring;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.monitoring.RegionConnectivity;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.monitoring.LatencyMonitoringUtility;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import lombok.RequiredArgsConstructor;

/**
 * This monitoring handler monitors registered regions. This includes checking if a regions is
 * reachable and measuring the latency to that region.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class RegionMonitoringHandler implements MonitoringHandler {
    private static final Logger logger = LoggerFactory.getLogger(RegionMonitoringHandler.class);

    private final Vertx vertx;

    private final ConfigDTO configDTO;

    private final ServiceProxyProvider serviceProxyProvider;

    private final LatencyMonitoringUtility latencyMonitoringUtility;

    private long currentTimer = -1L;

    @Override
    public void startMonitoringLoop() {
        long period = (long) (configDTO.getRegionMonitoringPeriod() * 1000);
        Handler<Long> monitoringHandler = id -> {
            logger.info("Started: monitor regions");
            serviceProxyProvider.getRegionService().findAllByProviderName(ResourceProviderEnum.AWS.getValue())
                .flatMapObservable(Observable::fromIterable)
                .map(region -> ((JsonObject) region).mapTo(Region.class))
                .flatMapSingle(region -> {
                    logger.info("Monitor latency: " + region.getName());
                    String pingUrl = latencyMonitoringUtility.getPingUrl(region);
                    return latencyMonitoringUtility.measureLatency(configDTO.getLatencyMonitoringCount(), pingUrl)
                        .map(processOutput -> {
                            RegionConnectivity connectivity = new RegionConnectivity();
                            connectivity.setRegion(region);
                            if (processOutput.getProcess().exitValue() == 0) {
                                connectivity.setIsOnline(true);
                                connectivity.setLatencySeconds(Double.parseDouble(processOutput.getOutput()) / 1000);
                            } else {
                                logger.info("Region " + region.getName() + " not reachable: " + processOutput.getOutput());
                                connectivity.setIsOnline(false);
                            }
                            return connectivity;
                        });
                })
                .toList()
                .map(connectivities -> {
                    JsonArray serializedConnectivities = new JsonArray(Json.encode(connectivities));
                    return serviceProxyProvider.getRegionMetricPushService()
                        .composeAndPushMetrics(serializedConnectivities)
                        .subscribe();
                })
                .subscribe(res -> logger.info("Finished: monitor regions"),
                    throwable -> logger.error(throwable.getMessage()));
        };
        currentTimer = vertx.setPeriodic(0L, period, monitoringHandler);
    }

    @Override
    public void pauseMonitoringLoop() {
        vertx.cancelTimer(currentTimer);
        currentTimer = -1L;
    }
}
