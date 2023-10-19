package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.monitoring.K8sMonitoringData;
import at.uibk.dps.rm.entity.monitoring.K8sMonitoringMetricEnum;
import at.uibk.dps.rm.entity.monitoring.K8sNode;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestMonitoringDataProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage.Session;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link K8sResourceUpdateUtility} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class K8sResourceUpdateUtilityTest {
    private K8sResourceUpdateUtility utility;

    @Mock
    private MetricRepository metricRepository;

    @Mock
    private Session session;

    private SessionManager sessionManager;


    private MainResource mr;

    private K8sMonitoringData monitoringData;

    private Metric mHostname, mCPU, mCPUAvailable, mMemory, mMemoryAvailable, mStorage, mStorageAvailable;


    @BeforeEach
    void initTest() {
        utility = new K8sResourceUpdateUtility(metricRepository);
        mr = TestResourceProvider.createClusterWithNodes(1L, "cluster", "n1",
            "n2", "n4");
        K8sNode k8sn1 = TestMonitoringDataProvider.createK8sNode("n1", 10.0, 8.75,
            1000, 500, 10000, 5000);
        K8sNode k8sn2 = TestMonitoringDataProvider.createK8sNode("n2", 5.0, 3.25,
            500, 200, 5000, 2000);
        K8sNode k8sn3 = TestMonitoringDataProvider.createK8sNode("n3", 2.0, 0.5,
            200, 100, 2000, 1000);
        V1Namespace namespace = TestMonitoringDataProvider.createV1Namespace("default");
        monitoringData = new K8sMonitoringData(List.of(k8sn1, k8sn2, k8sn3), List.of(namespace));
        mHostname = TestMetricProvider.createMetric(1L, K8sMonitoringMetricEnum.HOSTNAME.getName());
        mCPU = TestMetricProvider.createMetric(2L, K8sMonitoringMetricEnum.CPU.getName());
        mCPUAvailable = TestMetricProvider.createMetric(3L,
            K8sMonitoringMetricEnum.CPU_AVAILABLE.getName());
        mMemory = TestMetricProvider.createMetric(4L, K8sMonitoringMetricEnum.MEMORY_SIZE.getName());
        mMemoryAvailable = TestMetricProvider.createMetric(5L,
            K8sMonitoringMetricEnum.MEMORY_SIZE_AVAILABLE.getName());
        mStorage = TestMetricProvider.createMetric(6L, K8sMonitoringMetricEnum.STORAGE_SIZE.getName());
        mStorageAvailable = TestMetricProvider.createMetric(7L,
            K8sMonitoringMetricEnum.STORAGE_SIZE_AVAILABLE.getName());
        sessionManager = new SessionManager(session);
    }

    @Test
    void updateClusterNodes(VertxTestContext testContext) {
        when(session.remove(new Object[]{mr.getSubResources().get(2)})).thenReturn(CompletionStages.voidFuture());
        when(session.persist(any())).thenReturn(CompletionStages.voidFuture());
        when(metricRepository.findByMetric(sessionManager, K8sMonitoringMetricEnum.HOSTNAME.getName()))
            .thenReturn(Maybe.just(mHostname));
        when(metricRepository.findByMetric(sessionManager, K8sMonitoringMetricEnum.CPU.getName()))
            .thenReturn(Maybe.just(mCPU));
        when(metricRepository.findByMetric(sessionManager, K8sMonitoringMetricEnum.CPU_AVAILABLE.getName()))
            .thenReturn(Maybe.just(mCPUAvailable));
        when(metricRepository.findByMetric(sessionManager, K8sMonitoringMetricEnum.MEMORY_SIZE.getName()))
            .thenReturn(Maybe.just(mMemory));
        when(metricRepository.findByMetric(sessionManager, K8sMonitoringMetricEnum.MEMORY_SIZE_AVAILABLE.getName()))
            .thenReturn(Maybe.just(mMemoryAvailable));
        when(metricRepository.findByMetric(sessionManager, K8sMonitoringMetricEnum.STORAGE_SIZE.getName()))
            .thenReturn(Maybe.just(mStorage));
        when(metricRepository.findByMetric(sessionManager, K8sMonitoringMetricEnum.STORAGE_SIZE_AVAILABLE.getName()))
            .thenReturn(Maybe.just(mStorageAvailable));

        utility.updateClusterNodes(sessionManager, mr, monitoringData)
            .blockingSubscribe(() -> testContext.verify(() -> {
                    verify(session, times(8)).persist(any());
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void updateCluster(VertxTestContext testContext) {
        sessionManager = new SessionManager(session);
        when(session.persist(any())).thenReturn(CompletionStages.voidFuture());
        when(metricRepository.findByMetric(sessionManager, K8sMonitoringMetricEnum.CPU_AVAILABLE.getName()))
            .thenReturn(Maybe.just(mCPUAvailable));
        when(metricRepository.findByMetric(sessionManager, K8sMonitoringMetricEnum.MEMORY_SIZE_AVAILABLE.getName()))
            .thenReturn(Maybe.just(mMemoryAvailable));
        when(metricRepository.findByMetric(sessionManager, K8sMonitoringMetricEnum.STORAGE_SIZE_AVAILABLE.getName()))
            .thenReturn(Maybe.just(mStorageAvailable));

        utility.updateCluster(sessionManager, mr, monitoringData)
            .blockingSubscribe(() -> testContext.verify(() -> {
                    Set<MetricValue> metricValues = mr.getMetricValues();
                    metricValues.forEach(mv -> {
                        if (mv.getMetric().getMetric().equals(K8sMonitoringMetricEnum.CPU.getName())) {
                            assertThat(mv.getValueNumber().compareTo(BigDecimal.valueOf(17.0))).isEqualTo(0);
                        }
                        if (mv.getMetric().getMetric().equals(K8sMonitoringMetricEnum.MEMORY_SIZE.getName())) {
                            assertThat(mv.getValueNumber().compareTo(BigDecimal.valueOf(1700.0))).isEqualTo(0);
                        }
                        if (mv.getMetric().getMetric().equals(K8sMonitoringMetricEnum.STORAGE_SIZE.getName())) {
                            assertThat(mv.getValueNumber().compareTo(BigDecimal.valueOf(17000.0))).isEqualTo(0);
                        }
                    });
                    assertThat(metricValues.size()).isEqualTo(4);
                    verify(session, times(3)).persist(any());
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
