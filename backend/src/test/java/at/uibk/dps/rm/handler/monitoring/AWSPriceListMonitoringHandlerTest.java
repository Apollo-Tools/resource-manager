package at.uibk.dps.rm.handler.monitoring;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.monitoring.AWSPrice;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.monitoring.aws.EC2PriceMonitoring;
import at.uibk.dps.rm.service.monitoring.aws.LambdaPriceMonitoring;
import at.uibk.dps.rm.service.rxjava3.database.resource.PlatformService;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.RegionService;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricpusher.AWSPricePushService;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import kotlin.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link AWSPriceListMonitoringHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class AWSPriceListMonitoringHandlerTest {

    private AWSPriceListMonitoringHandler monitoringHandler;

    private Vertx spyVertx;

    @Mock
    private ServiceProxyProvider serviceProxyProvider;

    @Mock
    private PlatformService platformService;

    @Mock
    private RegionService regionService;

    @Mock
    private AWSPricePushService awsPricePushService;

    @Mock
    private LambdaPriceMonitoring lambdaPriceMonitoring;

    @Mock
    private EC2PriceMonitoring ec2PriceMonitoring;

    private static Logger logger;

    private static MockedStatic<LoggerFactory> mockedLoggerFactory;

    private Platform pLambda, pEC2;
    private Region reg1, reg2;

    private JsonArray regionList, platformList;

    @BeforeAll
    static void initAll() {
        logger = mock(Logger.class);
        mockedLoggerFactory = mockStatic(LoggerFactory.class);
        mockedLoggerFactory.when(() -> LoggerFactory.getLogger(AWSPriceListMonitoringHandler.class)).thenReturn(logger);
    }

    @AfterAll
    static void cleanupAll() {
        mockedLoggerFactory.close();
    }

    @BeforeEach
    void initTest(Vertx vertx) {
        clearInvocations(logger);
        JsonMapperConfig.configJsonMapper();
        ConfigDTO configDTO = TestConfigProvider.getConfigDTO();
        configDTO.setAwsPriceMonitoringPeriod(5.0);
        ResourceProvider rpAWS = TestResourceProviderProvider.createResourceProvider(1L, ResourceProviderEnum.AWS.getValue());
        ResourceProvider rpCustom = TestResourceProviderProvider
            .createResourceProvider(2L, ResourceProviderEnum.CUSTOM_CLOUD.getValue());
        reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1", rpAWS);
        reg2 = TestResourceProviderProvider.createRegion(2L, "us-east-2", rpAWS);
        Region reg3 = TestResourceProviderProvider.createRegion(3L, "us-east-3", rpCustom);
        regionList = new JsonArray(Json.encode(List.of(reg1, reg2, reg3)));
        pLambda = TestPlatformProvider.createPlatformFaas(1L, PlatformEnum.LAMBDA.getValue());
        pEC2 = TestPlatformProvider.createPlatformFaas(2L, PlatformEnum.EC2.getValue());
        Platform pK8s = TestPlatformProvider.createPlatformFaas(3L, PlatformEnum.K8S.getValue());
        platformList = new JsonArray(Json.encode(List.of(pEC2, pLambda, pK8s)));
        spyVertx = spy(vertx);
        monitoringHandler = new AWSPriceListMonitoringHandler(spyVertx, configDTO, serviceProxyProvider,
            lambdaPriceMonitoring, ec2PriceMonitoring);

        lenient().when(serviceProxyProvider.getPlatformService()).thenReturn(platformService);
        lenient().when(serviceProxyProvider.getRegionService()).thenReturn(regionService);
        lenient().when(serviceProxyProvider.getAwsPricePushService()).thenReturn(awsPricePushService);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testStartValidationLoopNoPlatforms(boolean pauseLoop, VertxTestContext testContext)
            throws InterruptedException {
        when(platformService.findAllByResourceProvider(ResourceProviderEnum.AWS.getValue()))
            .thenReturn(Single.just(new JsonArray()));
        when(awsPricePushService.composeAndPushMetrics(new JsonArray())).thenReturn(Completable.complete());

        monitoringHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        if (pauseLoop) {
            monitoringHandler.pauseMonitoringLoop();
            testContext.awaitCompletion(5, TimeUnit.SECONDS);
            verify(spyVertx).setTimer(eq(5000L), any());
            ArgumentCaptor<String> loggerInfo = ArgumentCaptor.forClass(String.class);
            verify(logger).info(loggerInfo.capture());
            assertThat(loggerInfo.getValue()).isEqualTo("Finished: monitor aws price list");
        } else {
            testContext.awaitCompletion(5, TimeUnit.SECONDS);
            verify(spyVertx, times(2)).setTimer(eq(5000L), any());
            ArgumentCaptor<String> loggerInfo = ArgumentCaptor.forClass(String.class);
            verify(logger, times(2)).info(loggerInfo.capture());
            assertThat(loggerInfo.getAllValues().get(0)).isEqualTo("Finished: monitor aws price list");
            assertThat(loggerInfo.getAllValues().get(1)).isEqualTo("Finished: monitor aws price list");
        }
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testStartValidationLoopError(boolean pauseLoop, VertxTestContext testContext)
            throws InterruptedException {
        when(platformService.findAllByResourceProvider(ResourceProviderEnum.AWS.getValue()))
            .thenReturn(Single.error(BadInputException::new));

        monitoringHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        if (pauseLoop) {
            monitoringHandler.pauseMonitoringLoop();
            testContext.awaitCompletion(5, TimeUnit.SECONDS);
            verify(spyVertx).setTimer(eq(5000L), any());
            ArgumentCaptor<String> loggerInfo = ArgumentCaptor.forClass(String.class);
            verify(logger).error(loggerInfo.capture());
            assertThat(loggerInfo.getValue()).isEqualTo("bad input");
        } else {
            testContext.awaitCompletion(5, TimeUnit.SECONDS);
            verify(spyVertx, times(2)).setTimer(eq(5000L), any());
            ArgumentCaptor<String> loggerInfo = ArgumentCaptor.forClass(String.class);
            verify(logger, times(2)).error(loggerInfo.capture());
            assertThat(loggerInfo.getAllValues().get(0)).isEqualTo("bad input");
            assertThat(loggerInfo.getAllValues().get(1)).isEqualTo("bad input");
        }
        testContext.completeNow();
    }

    @Test
    public void testStartValidationLoopValid(VertxTestContext testContext) throws InterruptedException {
        Pair<String, BigDecimal> ec2Micro = new Pair<>("t2.micro", BigDecimal.valueOf(2.55));
        Pair<String, BigDecimal> ec2Large = new Pair<>("t2.large", BigDecimal.valueOf(4.55));
        Pair<String, BigDecimal> lambda = new Pair<>("lambda", BigDecimal.valueOf(0.5));
        List<AWSPrice> priceList = new ArrayList<>();
        for (Region region : List.of(reg1, reg2)) {
            priceList.add(TestAWSPriceProvider.createAWSPrice(ec2Micro, region, pEC2));
            priceList.add(TestAWSPriceProvider.createAWSPrice(ec2Large, region, pEC2));
        }
        for (Region region : List.of(reg1, reg2)) {
            priceList.add(TestAWSPriceProvider.createAWSPrice(lambda, region, pLambda));
        }

        when(platformService.findAllByResourceProvider(ResourceProviderEnum.AWS.getValue()))
            .thenReturn(Single.just(platformList));
        when(regionService.findAll()).thenReturn(Single.just(regionList));
        when(ec2PriceMonitoring.computeExpectedPrice("https://pricing.us-east-1.amazonaws.com/offers/v1.0/" +
            "aws/AmazonEC2/current/us-east-1/index.json")).thenReturn(Single.just(List.of(ec2Micro, ec2Large)));
        when(ec2PriceMonitoring.computeExpectedPrice("https://pricing.us-east-1.amazonaws.com/offers/v1.0/" +
            "aws/AmazonEC2/current/us-east-2/index.json")).thenReturn(Single.just(List.of(ec2Micro, ec2Large)));
        when(lambdaPriceMonitoring.computeExpectedPrice("https://pricing.us-east-1.amazonaws.com/offers/v1.0/" +
            "aws/AWSLambda/current/us-east-1/index.json")).thenReturn(Single.just(List.of(lambda)));
        when(lambdaPriceMonitoring.computeExpectedPrice("https://pricing.us-east-1.amazonaws.com/offers/v1.0/" +
            "aws/AWSLambda/current/us-east-2/index.json")).thenReturn(Single.just(List.of(lambda)));
        when(awsPricePushService.composeAndPushMetrics(new JsonArray(Json.encode(priceList))))
            .thenReturn(Completable.complete());

        monitoringHandler.startMonitoringLoop();
        monitoringHandler.pauseMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx, times(1)).setTimer(eq(5000L), any());
        ArgumentCaptor<String> loggerInfo = ArgumentCaptor.forClass(String.class);
        verify(logger, times(3)).info(loggerInfo.capture());
        assertThat(loggerInfo.getAllValues().get(0)).isEqualTo("skipping platform k8s");
        assertThat(loggerInfo.getAllValues().get(1)).isEqualTo("skipping platform k8s");
        assertThat(loggerInfo.getAllValues().get(2)).isEqualTo("Finished: monitor aws price list");
        testContext.completeNow();
    }
}
