package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sMonitoringData;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sNode;
import at.uibk.dps.rm.testutil.objectprovider.TestMonitoringDataProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage.Session;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
    private Session session;

    private SessionManager sessionManager;

    private MainResource mr;

    private K8sMonitoringData monitoringData;


    @BeforeEach
    void initTest() {
        utility = new K8sResourceUpdateUtility();
        mr = TestResourceProvider.createClusterWithNodes(1L, "cluster", "n1",
            "n2", "n4");
        K8sNode k8sn1 = TestMonitoringDataProvider.createK8sNode("n1", 10.0, 8.75,
            1000, 500, 10000, 5000);
        K8sNode k8sn2 = TestMonitoringDataProvider.createK8sNode("n2", 5.0, 3.25,
            500, 200, 5000, 2000);
        K8sNode k8sn3 = TestMonitoringDataProvider.createK8sNode("n3", 2.0, 0.5,
            200, 100, 2000, 1000);
        V1Namespace namespace = TestMonitoringDataProvider.createV1Namespace("default");
        monitoringData = new K8sMonitoringData("cluster", "http://clusterurl:9999", 1L,
            List.of(k8sn1), List.of(namespace), true, 0.15);;
        sessionManager = new SessionManager(session);
    }

    // TODO: fix
    @Test
    void updateClusterNodes(VertxTestContext testContext) {
        when(session.remove(new Object[]{mr.getSubResources().get(2)})).thenReturn(CompletionStages.voidFuture());
        when(session.persist(any())).thenReturn(CompletionStages.voidFuture());

        utility.updateClusterNodes(sessionManager, mr, monitoringData)
            .blockingSubscribe(() -> testContext.verify(() -> {
                    verify(session, times(8)).persist(any());
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
