package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.service.database.util.SLOUtility;
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link MetricServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class MetricServiceImplTest {

    private MetricService metricService;

    @Mock
    private MetricRepository metricRepository;

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    private List<ServiceLevelObjective> slos;

    private Metric m1, m2, m3;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        metricService = new MetricServiceImpl(metricRepository, smProvider);
        m1 = TestMetricProvider.createMetric(1L, "availability");
        m2 = TestMetricProvider.createMetric(2L, "latency");
        m3 = TestMetricProvider.createMetric(3L, "cpu");
        ServiceLevelObjective slo1 = new ServiceLevelObjective(m1.getMetric(), ExpressionType.GT,
            TestDTOProvider.createSLOValueList(0.8));
        ServiceLevelObjective slo2 = new ServiceLevelObjective(m2.getMetric(), ExpressionType.LT,
            TestDTOProvider.createSLOValueList(0.2));
        ServiceLevelObjective slo3 = new ServiceLevelObjective(m3.getMetric(), ExpressionType.EQ,
            TestDTOProvider.createSLOValueList(1.0, 2.0));
        slos = List.of(slo1, slo2, slo3);
    }

    @Test
    public void checkMetricTypeForSLOsValid(VertxTestContext testContext) {
        JsonObject sloRequest = JsonObject.mapFrom(TestDTOProvider.createSLORequest(slos));
        List<Metric> metrics = List.of(m1, m2, m3);

        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(metricRepository.findAllBySLOs(sessionManager, slos)).thenReturn(Single.just(metrics));
        try (MockedStatic<SLOUtility> sloUtilityMock = mockStatic(SLOUtility.class)) {
            metricService.checkMetricTypeForSLOs(sloRequest,
                testContext.succeeding(result -> {
                    for (int i = 0; i < slos.size(); i++) {
                        int finalI = i;
                        sloUtilityMock.verify(() -> SLOUtility.validateSLOType(slos.get(finalI), metrics.get(finalI)));
                    }
                    testContext.verify(testContext::completeNow);
                }));
        }
    }

    @Test
    public void checkMetricTypeForSLOsMetricNotFound(VertxTestContext testContext) {
        JsonObject sloRequest = JsonObject.mapFrom(TestDTOProvider.createSLORequest(slos));
        List<Metric> metrics = List.of(m1, m2);

        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(metricRepository.findAllBySLOs(sessionManager, slos)).thenReturn(Single.just(metrics));
        try (MockedStatic<SLOUtility> sloUtilityMock = mockStatic(SLOUtility.class)) {
            metricService.checkMetricTypeForSLOs(sloRequest,
                testContext.failing(throwable -> {
                    for (int i = 0; i < slos.size() - 1; i++) {
                        int finalI = i;
                        sloUtilityMock.verify(() -> SLOUtility.validateSLOType(slos.get(finalI), metrics.get(finalI)));
                    }
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    assertThat(throwable.getMessage()).isEqualTo("request contains unknown metric");
                    testContext.completeNow();
                }));
        }

        testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(NotFoundException.class);
            testContext.completeNow();
        }));
    }
}
