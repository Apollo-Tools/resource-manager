package at.uibk.dps.rm.service.monitoring.metricpusher;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.AWSPrice;
import at.uibk.dps.rm.entity.monitoring.OpenTSDBEntity;
import at.uibk.dps.rm.service.ServiceProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is the implementation of the {@link FunctionInvocationPushService}.
 *
 * @author matthi-g
 */
public class AWSPricePushServiceImpl extends ServiceProxy implements AWSPricePushService {

    private final MetricPusher monitoringPusher;

    public AWSPricePushServiceImpl(WebClient webClient, ConfigDTO config) {
        monitoringPusher = new MetricPusher(webClient, config);
    }

    @Override
    public String getServiceProxyAddress() {
        return "aws-price-pusher" + super.getServiceProxyAddress();
    }

    public void composeAndPushMetrics(JsonArray awsPriceArray, Handler<AsyncResult<Void>> resultHandler) {
        AWSPrice[] awsPriceList = awsPriceArray.stream()
            .map(value -> ((JsonObject) value).mapTo(AWSPrice.class))
            .toArray(AWSPrice[]::new);
        List<OpenTSDBEntity> metrics = Arrays.stream(awsPriceList)
            .map(awsPrice -> new OpenTSDBEntity("aws_price_usd", awsPrice.getPrice().doubleValue(),
                Map.of("region", Long.toString(awsPrice.getRegion().getRegionId()),
                    "platform", Long.toString(awsPrice.getPlatform().getPlatformId()),
                    "instance_type", awsPrice.getInstanceType())))
            .collect(Collectors.toList());

        monitoringPusher.pushMetrics(metrics, resultHandler);
    }
}
