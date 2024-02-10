package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link SLOUtility} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class SLOUtilityTest {

    private SLOUtility utility;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private Stage.Session session;

    private SessionManager sessionManager;

    private Metric mAvailabilityNumber, mAvailabilityString;

    private Environment eCloud;

    private Platform pLambda, pEc2;

    private ResourceProvider rpAWS;

    private Region reg1, reg2;

    private ResourceType rtContainer;

    private Resource r1, r2, r3;

    @BeforeEach
    void initTest() {
        utility = new SLOUtility(resourceRepository);
        sessionManager = new SessionManager(session);
        eCloud = TestResourceProviderProvider.createEnvironment(1L, "cloud");
        rtContainer = TestResourceProvider.createResourceTypeFaas(5L);
        pLambda = TestPlatformProvider.createPlatformFaas(3L, "lambda");
        pEc2 = TestPlatformProvider.createPlatformFaas(4L, "ec2");
        rpAWS = TestResourceProviderProvider
            .createResourceProvider(5L, "aws", eCloud);
        reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1", rpAWS);
        reg2 = TestResourceProviderProvider.createRegion(2L, "us-east-2", rpAWS);
        MetricType mtNumber = TestMetricProvider.createMetricTypeNumber();
        MetricType mtString = TestMetricProvider.createMetricTypeString();
        mAvailabilityNumber = TestMetricProvider.createMetric(1L, "availability", mtNumber);
        mAvailabilityString = TestMetricProvider.createMetric(1L, "availability", mtString);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, mAvailabilityNumber, 0.85);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, mAvailabilityNumber, 0.71);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, mAvailabilityNumber, 0.95);
        r1 = TestResourceProvider.createResource(1L, pLambda, reg1);
        r1.getMetricValues().add(mv1);
        r2 = TestResourceProvider.createResource(2L, pEc2, reg2);
        r2.getMetricValues().add(mv2);
        r3 = TestResourceProvider.createResource(3L, pEc2, reg2);
        r3.getMetricValues().add(mv3);
    }

    // TODO: fix
    @Test
    void findResourcesByNonMonitoredSLOs(VertxTestContext testContext) {
        SLORequest sloRequest = TestDTOProvider.createSLORequest();

        when(resourceRepository.findAllByNonMVSLOs(sessionManager,List.of(eCloud.getEnvironmentId()),
            List.of(rtContainer.getTypeId()), List.of(pLambda.getPlatformId(),
                pEc2.getPlatformId()), List.of(reg1.getRegionId(), reg2.getRegionId()), List.of(rpAWS.getProviderId())))
            .thenReturn(Single.just(List.of(r1, r2, r3)));

        utility.findResourcesByNonMonitoredSLOs(sessionManager, sloRequest)
            .subscribe(resources -> testContext.verify(() -> {
                    assertThat(resources.size()).isEqualTo(2);
                    assertThat(resources.get(0).getResourceId()).isEqualTo(3L);
                    assertThat(resources.get(1).getResourceId()).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    // TODO: fix
    @Test
    void findResourcesByNonMonitoredSLOsNoneMatch(VertxTestContext testContext) {
        SLORequest sloRequest = TestDTOProvider.createSLORequest();

        when(resourceRepository.findAllBySLOs(sessionManager, List.of("availability"),
            List.of(eCloud.getEnvironmentId()), List.of(rtContainer.getTypeId()), List.of(pLambda.getPlatformId(),
                pEc2.getPlatformId()), List.of(reg1.getRegionId(), reg2.getRegionId()), List.of(rpAWS.getProviderId())))
            .thenReturn(Single.just(List.of()));

        utility.findResourcesByNonMonitoredSLOs(sessionManager, sloRequest)
            .subscribe(resources -> testContext.verify(() -> {
                    assertThat(resources.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    // TODO: fix
    @Test
    void findResourcesByNonMonitoredSLOsBadInputType(VertxTestContext testContext) {
        SLORequest sloRequest = TestDTOProvider.createSLORequest();

        utility.findResourcesByNonMonitoredSLOs(sessionManager, sloRequest)
            .subscribe(resources -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    assertThat(throwable.getMessage()).isEqualTo("bad input type for service level objective " +
                        "availability");
                    testContext.completeNow();
                })
            );
    }

    // TODO: fix
    @Test
    void findAndFilterResourcesBySLOsMetricNotFound(VertxTestContext testContext) {
        SLORequest sloRequest = TestDTOProvider.createSLORequest();

        utility.findResourcesByNonMonitoredSLOs(sessionManager, sloRequest)
            .subscribe(resources -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }


}
