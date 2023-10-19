package at.uibk.dps.rm.util.misc;

import at.uibk.dps.rm.entity.monitoring.K8sNode;
import at.uibk.dps.rm.testutil.objectprovider.TestMonitoringDataProvider;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link K8sDescribeParser} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class K8sDescribeParserTest {

    String describeContent = "Allocated resources:\n" +
        "  (Total limits may be over 100 percent, i.e., overcommitted.)\n" +
        "  Resource           Requests      Limits\n" +
        "  --------           --------      ------\n" +
        "  cpu                2350m (29%)   2100m (26%)\n" +
        "  memory             2128Mi (27%)  2128Mi (27%)\n" +
        "  ephemeral-storage  10m (2%)        10m (2%)\n" +
        "  hugepages-2Mi      20m (3%)        20m (3%)\n" +
        "Events:              <none>\n";

    @Test
    void parseContent() {
        K8sNode k8sNode = TestMonitoringDataProvider.createK8sNode("n1", 10.0, 0,
            1000, 0, 10000, 0);

        K8sDescribeParser.parseContent(describeContent, k8sNode);

        assertThat(k8sNode.getCpuLoad().getNumber().compareTo(BigDecimal.valueOf(2.35)))
            .isEqualTo(0);
        assertThat(k8sNode.getMemoryLoad().getNumber().compareTo(BigDecimal.valueOf(2231369728L)))
            .isEqualTo(0);
        assertThat(k8sNode.getStorageLoad().getNumber().compareTo(BigDecimal.valueOf(0.01)))
            .isEqualTo(0);
    }
}
