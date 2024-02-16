package at.uibk.dps.rm.service.monitoring.metricpusher;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.service.ServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.client.WebClient;

/**
 * The interface of the service proxy for pushing aws price list data to the monitoring
 * system.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface AWSPricePushService extends ServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static AWSPricePushService create(WebClient webClient, ConfigDTO config) {
        return new AWSPricePushServiceImpl(webClient, config);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static AWSPricePushService createProxy(Vertx vertx) {
        return new AWSPricePushServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(
            "aws-price-pusher"));
    }

    /**
     * Compose OpenTSDB metrics and push it to the monitoring system.
     *
     * @param awsPriceArray the price list of the registered aws arrays
     * @param resultHandler receives nothing if the saving was successful else an error
     */
    void composeAndPushMetrics(JsonArray awsPriceArray, Handler<AsyncResult<Void>> resultHandler);
}
