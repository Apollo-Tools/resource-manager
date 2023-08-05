package at.uibk.dps.rm.entity.monitoring;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.kubernetes.client.openapi.models.V1Node;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class K8sNode {

    private V1Node node;

    private int cpuLoad;

    private int memoryLoad;

    private long storageLoad;

    public K8sNode(V1Node node) {
        this.node = node;
    }

    @JsonIgnore
    public String getName() {
        return Objects.requireNonNull(node.getMetadata()).getName();
    }

    @JsonIgnore
    public String getHostname() {
        if (node.getMetadata() != null && node.getMetadata().getLabels() != null) {
            return node.getMetadata().getLabels().get("kubernetes.io/hostname");
        }
        throw new NullPointerException();
    }
    @JsonIgnore
    public BigDecimal getAllocatableCPU() {
        if (node.getStatus() != null && node.getStatus().getAllocatable() != null) {
            return node.getStatus().getAllocatable().get("cpu").getNumber();
        }
        throw new NullPointerException();
    }
    @JsonIgnore
    public BigDecimal getAllocatableMemory() {
        if (node.getStatus() != null && node.getStatus().getAllocatable() != null) {
            return node.getStatus().getAllocatable().get("memory").getNumber();
        }
        throw new NullPointerException();
    }
}
