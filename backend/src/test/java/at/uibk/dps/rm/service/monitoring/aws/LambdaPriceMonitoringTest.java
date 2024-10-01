package at.uibk.dps.rm.service.monitoring.aws;

import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.AWSPriceList;
import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.AWSPriceProduct;
import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.AWSPriceTerm;
import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.AWSPriceTerms;
import at.uibk.dps.rm.testutil.objectprovider.TestAWSPriceProvider;
import at.uibk.dps.rm.util.monitoring.ComputePriceUtility;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;
import io.vertx.rxjava3.ext.web.client.HttpResponse;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link LambdaPriceMonitoring} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class LambdaPriceMonitoringTest {

    @Mock
    private WebClient webClient;

    @Mock
    private ComputePriceUtility computePriceUtility;

    @Mock
    private HttpRequest<Buffer> mockRequest;

    @Mock
    private HttpResponse<Buffer> mockResponse;

    private LambdaPriceMonitoring lambdaPriceMonitoring;

    private static final String PRICE_URL = "https://test-url.com/prices";

    @BeforeEach
    void setUp() {
        lambdaPriceMonitoring = new LambdaPriceMonitoring(webClient, computePriceUtility);
    }

    @Test
    public void computeExpectedPrice(Vertx vertx, VertxTestContext testContext) {
        AWSPriceProduct p1 = TestAWSPriceProvider.createAWSPriceProduct("t1", "Compute Instance", "lambda",
            "Linux", null, "Shared", "NA" , "Local-usage");
        AWSPriceProduct p2 = TestAWSPriceProvider.createAWSPriceProduct("t2", "Serverless", "lambda",
            "Linux", null, "Shared", "NA" , "Local-usage");
        AWSPriceProduct p3 = TestAWSPriceProvider.createAWSPriceProduct("t3", "Compute Instance", "lambda",
            "Windows", null, "Shared", "NA" , "Global-usage");
        AWSPriceProduct p4 = TestAWSPriceProvider.createAWSPriceProduct("t4", "Compute Instance", "lambda",
            "Linux", "test", "Shared", "NA" , "DedicatedRes");
        p1.getAttributes().setGroup("AWS-Lambda-Duration");
        p2.getAttributes().setGroup("AWS-Lambda-Requests");
        p3.getAttributes().setGroup("AWS-Lambda-Duration");
        AWSPriceTerm t1 = TestAWSPriceProvider.createAwsPriceTerm("t1.t1", new BigDecimal("0.2"));
        AWSPriceTerm t2 = TestAWSPriceProvider.createAwsPriceTerm("t2.t2",new BigDecimal("0.3"));
        AWSPriceTerm t3 = TestAWSPriceProvider.createAwsPriceTerm("t3.t3",new BigDecimal("0.4"));
        AWSPriceTerm t4 = TestAWSPriceProvider.createAwsPriceTerm("t4.t4",new BigDecimal("0.5"));
        AWSPriceTerms awsPriceTerms = TestAWSPriceProvider.createAWSPriceTerms(Map.of("t1",
            Map.of("t1.t1", t1), "t2", Map.of("t2.t2", t2), "t3", Map.of("t3.t3", t3), "t4", Map.of("t4.t4", t4)));
        AWSPriceList awsPriceList = TestAWSPriceProvider.createAWSPriceList(Map.of("prod1", p1, "prod2", p2,
            "prod3", p3, "prod4", p4), awsPriceTerms);

        when(webClient.getAbs(PRICE_URL)).thenReturn(mockRequest);
        when(mockRequest.send()).thenReturn(Single.just(mockResponse));
        when(mockResponse.bodyAsJsonObject()).thenReturn(JsonObject.mapFrom(awsPriceList));

        try (MockedStatic<Vertx> mockedVertx = mockStatic(Vertx.class)) {
            mockedVertx.when(Vertx::currentContext).thenReturn(vertx.getOrCreateContext());
            when(computePriceUtility.computeLambdaPrice(any(), any())).thenReturn(new BigDecimal("0.2"));
            lambdaPriceMonitoring.computeExpectedPrice(PRICE_URL).subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(1);
                    assertThat(result.get(0).component2().compareTo(BigDecimal.valueOf(0.4))).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        }
    }
}
