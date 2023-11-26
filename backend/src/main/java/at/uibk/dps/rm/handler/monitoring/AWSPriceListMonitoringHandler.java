package at.uibk.dps.rm.handler.monitoring;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.AwsPrice;
import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.monitoring.aws.AWSPriceMonitoring;
import at.uibk.dps.rm.service.monitoring.aws.EC2PriceMonitoring;
import at.uibk.dps.rm.service.monitoring.aws.LambdaPriceMonitoring;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
            .flatMapCompletable(platform -> serviceProxyProvider.getRegionService().findAll()
                .flatMapObservable(Observable::fromIterable)
                .map(region -> ((JsonObject) region).mapTo(Region.class))
                .filter(region -> {
                    ResourceProviderEnum resourceProvider = ResourceProviderEnum
                        .fromString(region.getResourceProvider().getProvider());
                    return resourceProvider.equals(ResourceProviderEnum.AWS);
                })
                .flatMapSingle(region -> {
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
                        return Single.just(List.of());
                    }
                    String priceUrl = "https://pricing.us-east-1.amazonaws.com/offers/v1.0/aws/" + offersCode +
                    "/current/" + region.getName() + "/index.json";
                    return priceMonitoring.computeExpectedPrice(priceUrl)
                        .flatMapObservable(Observable::fromIterable)
                        .map(price -> {
                            AwsPrice awsPrice = new AwsPrice();
                            awsPrice.setRegion(region);
                            awsPrice.setInstanceType("lambda");
                            awsPrice.setPrice(price);
                            awsPrice.setPlatform(platform);
                            return awsPrice;
                        })
                        .toList();
                })
                .toList()
                .flatMapCompletable(connectivities -> {
                    return Completable.complete();
                }))
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

    }
}
