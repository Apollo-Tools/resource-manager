package at.uibk.dps.rm;

import at.uibk.dps.rm.entity.monitoring.opentsdb.OpenTSDBEntity;
import at.uibk.dps.rm.service.monitoring.promexport.MetricPusher;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;

import java.util.List;
import java.util.Map;

public class temp {

    public static void main(String[] args) {
        long value1 = 1L;
        long value2 = 34;
        OpenTSDBEntity e1 = new OpenTSDBEntity("test_metric_num", 69.42, Map.of("resource", "1", "function", "1",
            "test", "test"));
        OpenTSDBEntity e2 = new OpenTSDBEntity("test_metric_bool", value1, Map.of("resource", "2", "function", "2",
            "test", "test"));
        OpenTSDBEntity e3 = new OpenTSDBEntity("test_metric_int", value2, Map.of("resource", "2", "function", "1",
            "test", "test"));

        JsonMapperConfig.configJsonMapper();
        Vertx vertx = Vertx.vertx();
        new ConfigUtility(vertx).getConfigDTO().flatMapCompletable(config -> {
                WebClient webClient = WebClient.create(vertx);
                MetricPusher mp = new MetricPusher(webClient, config);
                return mp.pushMetrics(List.of(e1, e2, e3));
            })
            .blockingSubscribe();
    }
}
