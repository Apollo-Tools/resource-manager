package at.uibk.dps.rm.service.monitoring.aws;

import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.*;
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
import kotlin.Pair;
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
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link EC2PriceMonitoring} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class EC2PriceMonitoringTest {

    @Mock
    private WebClient webClient;

    @Mock
    private ComputePriceUtility computePriceUtility;

    @Mock
    private HttpRequest<Buffer> mockRequest;

    @Mock
    private HttpResponse<Buffer> mockResponse;

    private EC2PriceMonitoring ec2PriceMonitoring;

    private static final String PRICE_URL = "https://test-url.com/prices";

    @BeforeEach
    void setUp() {
        ec2PriceMonitoring = new EC2PriceMonitoring(webClient, computePriceUtility);
    }

    @Test
    public void computeExpectedPrice(Vertx vertx, VertxTestContext testContext) {
        AWSPriceProduct p1 = TestAWSPriceProvider.createAWSPriceProduct("t1", "Compute Instance", "t2.micro",
            "Linux", null, "Shared", "NA" , "DedicatedRes");
        AWSPriceProduct p2 = TestAWSPriceProvider.createAWSPriceProduct("t2", "Serverless", "t2.micro",
            "Linux", null, "Shared", "NA" , "DedicatedRes");
        AWSPriceProduct p3 = TestAWSPriceProvider.createAWSPriceProduct("t3", "Compute Instance", "t2.micro",
            "Windows", null, "Shared", "NA" , "DedicatedRes");
        AWSPriceProduct p4 = TestAWSPriceProvider.createAWSPriceProduct("t4", "Compute Instance", "t2.micro",
            "Linux", "test", "Shared", "NA" , "DedicatedRes");
        AWSPriceProduct p5 = TestAWSPriceProvider.createAWSPriceProduct("t1", "Compute Instance", "t2.micro",
            "Linux", null, "Unshared", "NA" , "DedicatedRes");
        AWSPriceProduct p6 = TestAWSPriceProvider.createAWSPriceProduct("t3", "Compute Instance", "t1.micro",
            "Linux", null, "Shared", "NA" , "DedicatedRes");
        AWSPriceProduct p7 = TestAWSPriceProvider.createAWSPriceProduct("t4", "Compute Instance", "t1.nano",
            "Linux", null, "Shared", "NA", "DedicatedRes");
        AWSPriceTerm t1 = TestAWSPriceProvider.createAwsPriceTerm("t1.t1", new BigDecimal("0.2"));
        AWSPriceTerm t2 = TestAWSPriceProvider.createAwsPriceTerm("t2.t2",new BigDecimal("0.3"));
        AWSPriceTerm t3 = TestAWSPriceProvider.createAwsPriceTerm("t3.t3",new BigDecimal("0.4"));
        AWSPriceTerm t4 = TestAWSPriceProvider.createAwsPriceTerm("t4.t4",new BigDecimal("0.5"));
        AWSPriceTerms awsPriceTerms = TestAWSPriceProvider.createAWSPriceTerms(Map.of("t1",
            Map.of("t1.t1", t1), "t2", Map.of("t2.t2", t2), "t3", Map.of("t3.t3", t3), "t4", Map.of("t4.t4", t4)));
        AWSPriceList awsPriceList = TestAWSPriceProvider.createAWSPriceList(Map.of("prod1", p1, "prod2", p2,
            "prod3", p3, "prod4", p4, "prod5", p5, "prod6", p6, "prod7", p7), awsPriceTerms);

        when(webClient.getAbs(PRICE_URL)).thenReturn(mockRequest);
        when(mockRequest.send()).thenReturn(Single.just(mockResponse));
        when(mockResponse.bodyAsJsonObject()).thenReturn(JsonObject.mapFrom(awsPriceList));

        try (MockedStatic<Vertx> mockedVertx = mockStatic(Vertx.class)) {
            mockedVertx.when(Vertx::currentContext).thenReturn(vertx.getOrCreateContext());
            when(computePriceUtility.computeEC2Price(any())).thenReturn(new BigDecimal("0.2"));
            ec2PriceMonitoring.computeExpectedPrice(PRICE_URL).subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    for (Pair<String, BigDecimal> entry : result) {
                        assertThat(entry.component2().compareTo(BigDecimal.valueOf(0.2))).isEqualTo(0);
                    }
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        }
    }
}
