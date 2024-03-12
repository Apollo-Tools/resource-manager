package at.uibk.dps.rm.handler.monitoring;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.monitoring.OpenFaasConnectivity;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.misc.MetricValueMapper;
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

import java.util.Map;

/**
 * This monitoring handler monitors OpenFaaS resources. Most of the valuable metrics are
 * monitored by the external monitoring system but the latency is not part of it.
 * This handler implements the measurement of the network latency between OpenFaaS resources and
 * the Resource Manager.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class OpenFaasMonitoringHandler implements MonitoringHandler {
    private static final Logger logger = LoggerFactory.getLogger(OpenFaasMonitoringHandler.class);

    private final Vertx vertx;

    private final ConfigDTO configDTO;

    private long currentTimer = -1L;

    @Override
    public void startMonitoringLoop() {
        long period = (long) (configDTO.getOpenfaasMonitoringPeriod() * 1000);
        ServiceProxyProvider serviceProxyProvider = new ServiceProxyProvider(vertx);
        Handler<Long> monitoringHandler = id -> serviceProxyProvider.getResourceService()
            .findAllByPlatform(PlatformEnum.OPENFAAS.getValue())
            .flatMapObservable(Observable::fromIterable)
            .map(resource -> ((JsonObject) resource).mapTo(Resource.class))
            .filter(resource -> {
                Map<String, MetricValue> metricValues = MetricValueMapper.mapMetricValues(resource.getMetricValues());
                return metricValues.containsKey("base-url");
            })
            .flatMapSingle(resource -> {
                logger.info("Monitor latency: " + resource.getName());
                Map<String, MetricValue> metricValues = MetricValueMapper.mapMetricValues(resource.getMetricValues());
                String pingUrl = LatencyMonitoringUtility.getPingUrl(metricValues.get("base-url").getValueString());
                return LatencyMonitoringUtility.measureLatency(configDTO.getLatencyMonitoringCount(), pingUrl)
                    .map(processOutput -> {
                        OpenFaasConnectivity connectivity = new OpenFaasConnectivity();
                        connectivity.setResourceId(resource.getResourceId());
                        if (processOutput.getProcess().exitValue() == 0) {
                            connectivity.setLatencySeconds(Double.parseDouble(processOutput.getOutput()) / 1000);
                        } else {
                            logger.info("Resource " + resource.getName() + " not reachable: " +
                                processOutput.getOutput());
                        }
                        return connectivity;
                    });
            })
            .filter(connectivity -> connectivity.getLatencySeconds() != null)
            .toList()
            .map(connectivities -> {
                JsonArray serializedConnectivities = new JsonArray(Json.encode(connectivities));
                return serviceProxyProvider.getOpenFaasMetricPushService()
                    .composeAndPushMetrics(serializedConnectivities)
                    .subscribe();
            })
            .subscribe(res -> logger.info("Finished: monitor openfaas resources"),
                throwable -> logger.error(throwable.getMessage()));
        currentTimer = vertx.setPeriodic(period, monitoringHandler);
    }

    @Override
    public void pauseMonitoringLoop() {
        vertx.cancelTimer(currentTimer);
        currentTimer = -1L;
    }
}
