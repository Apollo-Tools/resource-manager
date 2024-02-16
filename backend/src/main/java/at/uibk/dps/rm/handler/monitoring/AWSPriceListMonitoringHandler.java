package at.uibk.dps.rm.handler.monitoring;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.monitoring.AWSPrice;
import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.monitoring.aws.AWSPriceMonitoring;
import at.uibk.dps.rm.service.monitoring.aws.EC2PriceMonitoring;
import at.uibk.dps.rm.service.monitoring.aws.LambdaPriceMonitoring;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import lombok.RequiredArgsConstructor;

/**
 * This monitoring handler monitors the aws price list api and persists the observed prices.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class AWSPriceListMonitoringHandler implements MonitoringHandler {

    private static final Logger logger = LoggerFactory.getLogger(AWSPriceListMonitoringHandler.class);

    private final Vertx vertx;

    private final WebClient webClient;

    private final ConfigDTO configDTO;

    private long currentTimer = -1L;

    private boolean pauseLoop = false;

    private static Handler<Long> monitoringHandler;


    @Override
    public void startMonitoringLoop() {
        pauseLoop = false;
        long period = (long) (configDTO.getAwsPriceMonitoringPeriod() * 60 * 1000);
        ServiceProxyProvider serviceProxyProvider = new ServiceProxyProvider(vertx);
        LambdaPriceMonitoring lambdaMonitoring = new LambdaPriceMonitoring(webClient);
        EC2PriceMonitoring ec2PriceMonitoring = new EC2PriceMonitoring(webClient);
        monitoringHandler = id -> serviceProxyProvider.getPlatformService()
            .findAllByResourceProvider(ResourceProviderEnum.AWS.getValue())
            .flatMapObservable(Observable::fromIterable)
            .map(platform -> ((JsonObject) platform).mapTo(Platform.class))
            .flatMap(platform -> serviceProxyProvider.getRegionService().findAll()
                .flatMapObservable(Observable::fromIterable)
                .map(region -> ((JsonObject) region).mapTo(Region.class))
                .filter(region -> {
                    ResourceProviderEnum resourceProvider = ResourceProviderEnum
                        .fromString(region.getResourceProvider().getProvider());
                    return resourceProvider.equals(ResourceProviderEnum.AWS);
                })
                .flatMap(region -> {
                    String offersCode;
                    PlatformEnum platformEnum = PlatformEnum.fromPlatform(platform);
                    AWSPriceMonitoring priceMonitoring;
                    if (platformEnum.equals(PlatformEnum.EC2)) {
                        offersCode = "AmazonEC2";
                        priceMonitoring = ec2PriceMonitoring;
                    } else if (platformEnum.equals(PlatformEnum.LAMBDA)) {
                        offersCode = "AWSLambda";
                        priceMonitoring = lambdaMonitoring;
                    } else {
                        logger.info("skipping platform " + platform.getPlatform());
                        return Observable.empty();
                    }
                    String priceUrl = "https://pricing.us-east-1.amazonaws.com/offers/v1.0/aws/" + offersCode +
                    "/current/" + region.getName() + "/index.json";
                    return priceMonitoring.computeExpectedPrice(priceUrl)
                        .flatMapObservable(Observable::fromIterable)
                        .map(pricePair -> {
                            AWSPrice awsPrice = new AWSPrice();
                            awsPrice.setRegion(region);
                            awsPrice.setInstanceType(pricePair.component1());
                            awsPrice.setPrice(pricePair.component2());
                            awsPrice.setPlatform(platform);
                            return awsPrice;
                        });
                })
            )
            .toList()
            .flatMapCompletable(prices -> {
                JsonArray serializedPrices = new JsonArray(Json.encode(prices));
                return serviceProxyProvider.getAwsPricePushService().composeAndPushMetrics(serializedPrices);
            })
            .subscribe(() -> {
                logger.info("Finished: monitor aws price list");
                currentTimer = pauseLoop ? currentTimer : vertx.setTimer(period, monitoringHandler);
            }, throwable -> {
                logger.error(throwable.getMessage());
                currentTimer = pauseLoop ? currentTimer : vertx.setTimer(period, monitoringHandler);
            });
        currentTimer = vertx.setTimer(period, monitoringHandler);
    }

    @Override
    public void pauseMonitoringLoop() {
        pauseLoop = true;
        if (!vertx.cancelTimer(currentTimer)) {
            vertx.cancelTimer(currentTimer);
        }
        currentTimer = -1L;
    }
}
