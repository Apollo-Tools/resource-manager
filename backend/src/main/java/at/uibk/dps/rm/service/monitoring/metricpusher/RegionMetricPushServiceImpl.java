package at.uibk.dps.rm.service.monitoring.metricpusher;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.RegionConnectivity;
import at.uibk.dps.rm.entity.monitoring.OpenTSDBEntity;
import at.uibk.dps.rm.service.ServiceProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.apache.commons.lang3.BooleanUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is the implementation of the {@link RegionMetricPushService}.
 *
 * @author matthi-g
 */
public class RegionMetricPushServiceImpl extends ServiceProxy implements RegionMetricPushService {

    private final MetricPusher monitoringPusher;

    public RegionMetricPushServiceImpl(WebClient webClient, ConfigDTO config) {
        monitoringPusher = new MetricPusher(webClient, config);
    }

    @Override
    public String getServiceProxyAddress() {
        return "region-metric-pusher" + super.getServiceProxyAddress();
    }

    public void composeAndPushMetrics(JsonArray regionConnectivity, Handler<AsyncResult<Void>> resultHandler) {
        RegionConnectivity[] regionConnectivityList = regionConnectivity.stream()
            .map(value -> ((JsonObject) value).mapTo(RegionConnectivity.class))
            .toArray(RegionConnectivity[]::new);
        List<OpenTSDBEntity> metrics = Arrays.stream(regionConnectivityList)
            .map(connectivity -> {
                List<OpenTSDBEntity> openTSDBEntities = new ArrayList<>();
                if (connectivity.getIsOnline()) {
                    openTSDBEntities.add(new OpenTSDBEntity("region_latency_seconds",
                        connectivity.getLatencySeconds(),
                        Map.of("region", Long.toString(connectivity.getRegion().getRegionId()))));
                }
                openTSDBEntities.add(new OpenTSDBEntity("region_up",
                    BooleanUtils.toInteger(connectivity.getIsOnline()),
                    Map.of("region", Long.toString(connectivity.getRegion().getRegionId()))));
                return openTSDBEntities;
            })
            .flatMap(List::stream)
            .collect(Collectors.toList());

        monitoringPusher.pushMetrics(metrics, resultHandler);
    }
}
