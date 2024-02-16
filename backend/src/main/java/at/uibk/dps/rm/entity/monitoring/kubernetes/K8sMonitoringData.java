package at.uibk.dps.rm.entity.monitoring.kubernetes;

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

    private String name;

    private String basePath;

    private long resourceId;

    private List<K8sNode> nodes;

    private List<V1Namespace> namespaces;

    private boolean isUp;

    private Double latencySeconds;

    /**
     * Create an instance with a JsonObject.
     *
     * @param jsonObject the JsonObject
     */
    public K8sMonitoringData(JsonObject jsonObject) {
        K8sMonitoringData request = jsonObject.mapTo(K8sMonitoringData.class);
        this.name = request.getName();
        this.basePath = request.getBasePath();
        this.resourceId = request.getResourceId();
        this.nodes = request.getNodes();
        this.namespaces = request.getNamespaces();
        this.isUp = request.getIsUp();
        this.latencySeconds = request.getLatencySeconds();
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
    public BigDecimal getCPUUsed() {
        return nodes.stream()
            .map(K8sNode::getCPUUsed)
            .reduce(BigDecimal.valueOf(0), BigDecimal::add);
    }

    @Override
    @JsonIgnore
    public BigDecimal getMemoryUsed() {
        return nodes.stream()
            .map(K8sNode::getMemoryUsed)
            .reduce(BigDecimal.valueOf(0), BigDecimal::add);
    }

    @Override
    @JsonIgnore
    public BigDecimal getStorageUsed() {
        return nodes.stream()
            .map(K8sNode::getStorageUsed)
            .reduce(BigDecimal.valueOf(0), BigDecimal::add);
    }
}
