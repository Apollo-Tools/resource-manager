package at.uibk.dps.rm.service.monitoring.aws;

import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.AWSPriceList;
import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.AWSPriceProduct;
import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.AWSPriceTerm;
import at.uibk.dps.rm.entity.dto.monitoring.awspricelist.AWSPriceTerms;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.model.SubResource;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
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
    private HttpRequest<Buffer> mockRequest;

    @Mock
    private HttpResponse<Buffer> mockResponse;

    private EC2PriceMonitoring ec2PriceMonitoring;

    private static final String PRICE_URL = "https://test-url.com/prices";

    private AWSPriceProduct p1, p2, p3, p4, p5, p6, p7, p8;

    @BeforeEach
    void setUp() {
        ec2PriceMonitoring = new EC2PriceMonitoring(webClient);
    }

    @Test
    public void computeExpectedPrice(Vertx vertx, VertxTestContext testContext) {
        p1 = TestAWSPriceProvider.createAWSPriceProduct("Compute Instance", "t2.micro",
            "Linux", null, "Shared", "NA" , "DedicatedRes");
        p2 = TestAWSPriceProvider.createAWSPriceProduct("Serverless", "t2.micro",
            "Linux", null, "Shared", "NA" , "DedicatedRes");
        p3 = TestAWSPriceProvider.createAWSPriceProduct("Compute Instance", "t2.micro",
            "Windows", null, "Shared", "NA" , "DedicatedRes");
        p4 = TestAWSPriceProvider.createAWSPriceProduct("Compute Instance", "t2.micro",
            "Linux", "test", "Shared", "NA" , "DedicatedRes");
        p5 = TestAWSPriceProvider.createAWSPriceProduct("Compute Instance", "t2.micro",
            "Linux", null, "Unshared", "NA" , "DedicatedRes");
        p6 = TestAWSPriceProvider.createAWSPriceProduct("Compute Instance", "t2.micro",
            "Linux", null, "Shared", "NA" , "DedicatedRes");
        p7 = TestAWSPriceProvider.createAWSPriceProduct("Compute Instance", "t1.micro",
            "Linux", null, "Shared", "NA" , "DedicatedRes");
        AWSPriceTerm t1 = TestAWSPriceProvider.createAwsPriceTerm("t1", new BigDecimal("0.2"));
        AWSPriceTerm t2 = TestAWSPriceProvider.createAwsPriceTerm("t2",new BigDecimal("0.3"));
        AWSPriceTerm t3 = TestAWSPriceProvider.createAwsPriceTerm("t3",new BigDecimal("0.4"));
        AWSPriceTerm t4 = TestAWSPriceProvider.createAwsPriceTerm("t4",new BigDecimal("0.5"));
        AWSPriceTerms awsPriceTerms = TestAWSPriceProvider.createAWSPriceTerms(Map.of("t1",
            Map.of("t1.t1", t1, "t1.t2", t2), "t3", Map.of("t3.t3", t3), "t4", Map.of("t4.t4", t4)));
        AWSPriceList awsPriceList = TestAWSPriceProvider.createAWSPriceList(Map.of("prod1", p1, "prod2", p2,
            "prod3", p3, "prod4", p4, "prod5", p5, "prod6", p6, "prod7", p7), awsPriceTerms);

        when(webClient.getAbs(PRICE_URL)).thenReturn(mockRequest);
        when(mockRequest.send()).thenReturn(Single.just(mockResponse));
        when(mockResponse.bodyAsJsonObject()).thenReturn(JsonObject.mapFrom(awsPriceList));

        try (MockedStatic<Vertx> mockedVertx = mockStatic(Vertx.class);
                MockedStatic<ComputePriceUtility> mockedUtility = mockStatic(ComputePriceUtility.class)) {
            mockedVertx.when(Vertx::currentContext).thenReturn(vertx.getOrCreateContext());
            mockedUtility.when(() -> ComputePriceUtility.computerEC2Price(t1.getPriceDimensions().get("t1")))
                .thenReturn(new BigDecimal("0.2"));
            mockedUtility.when(() -> ComputePriceUtility.computerEC2Price(t2.getPriceDimensions().get("t2")))
                .thenReturn(new BigDecimal("0.3"));
            mockedUtility.when(() -> ComputePriceUtility.computerEC2Price(t1.getPriceDimensions().get("t3")))
                .thenReturn(new BigDecimal("0.4"));
            mockedUtility.when(() -> ComputePriceUtility.computerEC2Price(t1.getPriceDimensions().get("t4")))
                .thenReturn(new BigDecimal("0.5"));
            ec2PriceMonitoring.computeExpectedPrice(PRICE_URL).subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> throwable.printStackTrace())
            );
        }
    }

    /*
    // Helper method to create a mock AWSPriceList
    private AWSPriceList createMockAWSPriceList() {
        AWSPriceProduct mockProduct = new AWSPriceProduct();
        mockProduct.setAttributes(new AWSPriceAttributes("Linux", "t2.micro", null, "NA", "Shared"));
        mockProduct.setProductFamily("Compute Instance");

        AWSPriceList mockPriceList = new AWSPriceList();
        mockPriceList.setProducts(Collections.singletonMap("prod1", mockProduct));

        AWSPriceTerm mockTerm = new AWSPriceTerm();
        mockTerm.setBeginRange("0");

        AWSPriceDimensions mockDimensions = new AWSPriceDimensions();
        mockDimensions.setPricePerUnit(new BigDecimal("0.0116"));

        mockTerm.setPriceDimensions(Collections.singletonMap("term1", mockDimensions));

        mockPriceList.setTerms(new AWSTerms(Collections.singletonMap("onDemand", mockTerm)));

        return mockPriceList;
    }*/
}
