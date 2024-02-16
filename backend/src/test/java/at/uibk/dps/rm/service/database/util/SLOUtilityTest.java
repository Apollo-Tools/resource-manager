package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        Metric mAvailabilityNumber = TestMetricProvider.createMetric(1L, "availability", mtNumber);
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

    private static Stream<Arguments> provideFindResources() {
        return Stream.of(
            Arguments.of(List.of(1L), List.of(5L), List.of(3L, 4L), List.of(1L, 2L), List.of(5L)),
            Arguments.of(List.of(), List.of(5L), List.of(3L, 4L), List.of(1L, 2L), List.of(5L)),
            Arguments.of(List.of(1L), List.of(), List.of(3L, 4L), List.of(1L, 2L), List.of(5L)),
            Arguments.of(List.of(1L), List.of(5L), List.of(), List.of(1L, 2L), List.of(5L)),
            Arguments.of(List.of(1L), List.of(5L), List.of(3L, 4L), List.of(), List.of(5L)),
            Arguments.of(List.of(1L), List.of(5L), List.of(3L, 4L), List.of(1L, 2L), List.of()),
            Arguments.of(List.of(), List.of(), List.of(3L, 4L), List.of(1L, 2L), List.of(5L)),
            Arguments.of(List.of(1L), List.of(), List.of(), List.of(1L, 2L), List.of(5L)),
            Arguments.of(List.of(1L), List.of(5L), List.of(), List.of(), List.of(5L)),
            Arguments.of(List.of(1L), List.of(5L), List.of(3L, 4L), List.of(), List.of()),
            Arguments.of(List.of(), List.of(), List.of(), List.of(1L, 2L), List.of(5L)),
            Arguments.of(List.of(1L), List.of(), List.of(), List.of(), List.of(5L)),
            Arguments.of(List.of(1L), List.of(5L), List.of(), List.of(), List.of()),
            Arguments.of(List.of(), List.of(), List.of(), List.of(), List.of(5L)),
            Arguments.of(List.of(1L), List.of(), List.of(), List.of(), List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindResources")
    void findResourcesByNonMonitoredSLOs(List<Long> environments, List<Long> resourceTypes, List<Long> platforms,
            List<Long> regions, List<Long> providers, VertxTestContext testContext) {
        SLORequest sloRequest = TestDTOProvider.createSLORequest();
        sloRequest.setEnvironments(environments);
        sloRequest.setResourceTypes(resourceTypes);
        sloRequest.setPlatforms(platforms);
        sloRequest.setRegions(regions);
        sloRequest.setProviders(providers);

        when(resourceRepository.findAllByNonMVSLOs(sessionManager, environments, resourceTypes, platforms, regions,
            providers)).thenReturn(Single.just(List.of(r1, r2, r3)));

        utility.findResourcesByNonMonitoredSLOs(sessionManager, sloRequest)
            .subscribe(resources -> testContext.verify(() -> {
                    assertThat(resources.size()).isEqualTo(3);
                    assertThat(resources.get(0).getResourceId()).isEqualTo(1L);
                    assertThat(resources.get(1).getResourceId()).isEqualTo(2L);
                    assertThat(resources.get(2).getResourceId()).isEqualTo(3L);
                    testContext.completeNow();
                }),
                throwable -> testContext.failNow("method has thrown exception")
            );
    }

    @Test
    void findResourcesByNonMonitoredSLOsNoneMatch(VertxTestContext testContext) {
        SLORequest sloRequest = TestDTOProvider.createSLORequest();

        when(resourceRepository.findAllByNonMVSLOs(sessionManager,List.of(eCloud.getEnvironmentId()),
            List.of(rtContainer.getTypeId()), List.of(pLambda.getPlatformId(),
                pEc2.getPlatformId()), List.of(reg1.getRegionId(), reg2.getRegionId()), List.of(rpAWS.getProviderId())))
            .thenReturn(Single.just(List.of()));

        utility.findResourcesByNonMonitoredSLOs(sessionManager, sloRequest)
            .subscribe(resources -> testContext.verify(() -> {
                    assertThat(resources.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.failNow("method has thrown exception")
            );
    }

    @Test
    void findResourcesByNonMonitoredSLOsNoNoneMonitoredSLOs(VertxTestContext testContext) {
        SLORequest sloRequest = TestDTOProvider.createSLORequest();
        sloRequest.setEnvironments(List.of());
        sloRequest.setResourceTypes(List.of());
        sloRequest.setPlatforms(List.of());
        sloRequest.setRegions(List.of());
        sloRequest.setProviders(List.of());

        when(resourceRepository.findAllMainAndSubResourcesAndFetch(sessionManager))
            .thenReturn(Single.just(List.of(r1, r2, r3)));

        utility.findResourcesByNonMonitoredSLOs(sessionManager, sloRequest)
            .subscribe(resources -> testContext.verify(() -> {
                    assertThat(resources.size()).isEqualTo(3);
                    assertThat(resources.get(0).getResourceId()).isEqualTo(1L);
                    assertThat(resources.get(1).getResourceId()).isEqualTo(2L);
                    assertThat(resources.get(2).getResourceId()).isEqualTo(3L);
                    testContext.completeNow();
                }),
                throwable -> testContext.failNow("method has thrown exception")
            );
    }

    private static Stream<Arguments> provideValidateSLOType() {
        ServiceLevelObjective slo1 = TestDTOProvider
            .createServiceLevelObjective("availability", ExpressionType.EQ, 0.5);
        ServiceLevelObjective slo2 = TestDTOProvider
            .createServiceLevelObjective("availability", ExpressionType.EQ, "0.5");
        ServiceLevelObjective slo3 = TestDTOProvider
            .createServiceLevelObjective("availability", ExpressionType.EQ, false);
        MetricType metricTypeNumber = TestMetricProvider.createMetricTypeNumber();
        MetricType metricTypeString = TestMetricProvider.createMetricTypeString();
        MetricType metricTypeBoolean = TestMetricProvider.createMetricTypeBoolean();
        Metric m1 = TestMetricProvider.createMetric(1L, "availability", metricTypeNumber);
        Metric m2 = TestMetricProvider.createMetric(1L, "availability", metricTypeString);
        Metric m3 = TestMetricProvider.createMetric(1L, "availability", metricTypeBoolean);

        return Stream.of(
            Arguments.of(slo1, m1, true),
            Arguments.of(slo2, m2, true),
            Arguments.of(slo3, m3, true),
            Arguments.of(slo1, m2, false),
            Arguments.of(slo2, m3, false),
            Arguments.of(slo3, m1, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidateSLOType")
    void validateSLOType(ServiceLevelObjective slo, Metric metric, boolean isValid) {
        if (isValid) {
            assertDoesNotThrow(() -> SLOUtility.validateSLOType(slo, metric));
        } else {
            BadInputException exception = assertThrows(BadInputException.class, () -> SLOUtility.validateSLOType(slo,
                metric));
            assertThat(exception.getMessage()).isEqualTo("bad input type for service level objective " + slo.getName());
        }
    }
}
