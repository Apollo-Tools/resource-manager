package at.uibk.dps.rm.entity.monitoring;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implements the monitored {@link K8sEntityData} for a k8s cluster resource.
 *
 * @author matthi-g
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DataObject
public class K8sMonitoringData implements K8sEntityData {

    private long resourceId;

    private List<K8sNode> nodes;

    private List<V1Namespace> namespaces;

    private boolean isUp;

    /**
     * Create an instance with a JsonObject.
     *
     * @param jsonObject the JsonObject
     */
    public K8sMonitoringData(JsonObject jsonObject) {
        K8sMonitoringData request = jsonObject.mapTo(K8sMonitoringData.class);
        this.resourceId = request.getResourceId();
        this.nodes = request.getNodes();
        this.namespaces = request.getNamespaces();
        this.isUp = request.getIsUp();
    }

    /**
     * Get the object as a JsonObject.
     *
     * @return the object as JsonObject
     */
    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }


    @Override
    @JsonIgnore
    public BigDecimal getTotalCPU() {
        return nodes.stream()
            .map(K8sNode::getTotalCPU)
            .reduce(BigDecimal.valueOf(0), BigDecimal::add);
    }

    @Override
    @JsonIgnore
    public BigDecimal getTotalMemory() {
        return nodes.stream()
            .map(K8sNode::getTotalMemory)
            .reduce(BigDecimal.valueOf(0), BigDecimal::add);
    }

    @Override
    @JsonIgnore
    public BigDecimal getTotalStorage() {
        return nodes.stream()
            .map(K8sNode::getTotalStorage)
            .reduce(BigDecimal.valueOf(0), BigDecimal::add);
    }

    @Override
    @JsonIgnore
    public BigDecimal getAvailableCPU() {
        return nodes.stream()
            .map(K8sNode::getAvailableCPU)
            .reduce(BigDecimal.valueOf(0), BigDecimal::add);
    }

    @Override
    @JsonIgnore
    public BigDecimal getAvailableMemory() {
        return nodes.stream()
            .map(K8sNode::getAvailableMemory)
            .reduce(BigDecimal.valueOf(0), BigDecimal::add);
    }

    @Override
    @JsonIgnore
    public BigDecimal getAvailableStorage() {
        return nodes.stream()
            .map(K8sNode::getAvailableStorage)
            .reduce(BigDecimal.valueOf(0), BigDecimal::add);
    }
}
