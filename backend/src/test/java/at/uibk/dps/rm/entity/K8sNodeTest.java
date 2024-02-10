package at.uibk.dps.rm.entity;

import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sNode;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeStatus;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Implements tests for the {@link K8sNode} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
public class K8sNodeTest {

    private long resourceId;

    private V1Node nValid, nInvalid;

    private Quantity quantity;

    @BeforeEach
    void initTest() {
        resourceId = 1L;
        V1ObjectMeta mdValid = new V1ObjectMeta();
        mdValid.setName("nodeName");
        mdValid.setLabels(Map.of("kubernetes.io/hostname", "node"));
        Map<String, Quantity> aValid = Map.of("cpu", new Quantity("11.0"), "memory", new Quantity("12.0"),
            "ephemeral-storage", new Quantity("13.0"));
        V1NodeStatus nsValid = new V1NodeStatus();
        nsValid.setAllocatable(aValid);
        nInvalid = new V1Node();
        nValid = new V1Node();
        nValid.setMetadata(mdValid);
        nValid.setStatus(nsValid);
        quantity = new Quantity("10.0");
    }

    @Test
    void getNameValid() {
        String result = new K8sNode(nValid).getName();

        assertThat(result).isEqualTo("nodeName");
    }

    @Test
    void getNameInvalid() {
        K8sNode k8sNode = new K8sNode(nInvalid);

        assertThrows(NullPointerException.class, k8sNode::getName);
    }

    @Test
    void getHostnameValid() {
        String result = new K8sNode(nValid).getHostname();

        assertThat(result).isEqualTo("node");
    }

    @ParameterizedTest
    @ValueSource(strings = {"mdNull", "labelsNull"})
    void getHostnameInvalid(String type) {
        if (type.equals("labelsNull")) {
            nInvalid.setMetadata(new V1ObjectMeta());
        }
        K8sNode k8sNode = new K8sNode(nInvalid);

        assertThrows(NullPointerException.class, k8sNode::getHostname);
    }

    @Test
    void getTotalCPUValid() {
        BigDecimal result = new K8sNode(nValid).getTotalCPU();

        assertThat(result.compareTo(BigDecimal.valueOf(11.0))).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"nsNull", "allocatableNull"})
    void getTotalCPUInvalid(String type) {
        if (type.equals("allocatableNull")) {
            nInvalid.setStatus(new V1NodeStatus());
        }
        K8sNode k8sNode = new K8sNode(nInvalid);

        assertThrows(NullPointerException.class, k8sNode::getTotalCPU);
    }

    @Test
    void getTotalMemoryValid() {
        BigDecimal result = new K8sNode(nValid).getTotalMemory();

        assertThat(result.compareTo(BigDecimal.valueOf(12.0))).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"nsNull", "allocatableNull"})
    void getTotalMemoryInvalid(String type) {
        if (type.equals("allocatableNull")) {
            nInvalid.setStatus(new V1NodeStatus());
        }
        K8sNode k8sNode = new K8sNode(nInvalid);

        assertThrows(NullPointerException.class, k8sNode::getTotalMemory);
    }

    @Test
    void getTotalStorageValid() {
        BigDecimal result = new K8sNode(nValid).getTotalStorage();

        assertThat(result.compareTo(BigDecimal.valueOf(13.0))).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"nsNull", "allocatableNull"})
    void getTotalStorageInvalid(String type) {
        if (type.equals("allocatableNull")) {
            nInvalid.setStatus(new V1NodeStatus());
        }
        K8sNode k8sNode = new K8sNode(nInvalid);

        assertThrows(NullPointerException.class, k8sNode::getTotalStorage);
    }

    @Test
    void getUsedCPU() {
        K8sNode k8sNode = new K8sNode(resourceId, nValid, Map.of(), quantity, quantity, quantity);

        BigDecimal result = k8sNode.getCPUUsed();

        assertThat(result.compareTo(BigDecimal.valueOf(1.0))).isEqualTo(0);
    }

    @Test
    void getUsedMemory() {
        K8sNode k8sNode = new K8sNode(resourceId, nValid, Map.of(), quantity, quantity, quantity);

        BigDecimal result = k8sNode.getMemoryUsed();

        assertThat(result.compareTo(BigDecimal.valueOf(2.0))).isEqualTo(0);
    }

    @Test
    void getUsedStorage() {
        K8sNode k8sNode = new K8sNode(resourceId, nValid, Map.of(), quantity, quantity, quantity);

        BigDecimal result = k8sNode.getStorageUsed();

        assertThat(result.compareTo(BigDecimal.valueOf(3.0))).isEqualTo(0);
    }
}
