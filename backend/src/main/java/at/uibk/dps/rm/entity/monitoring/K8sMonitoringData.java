package at.uibk.dps.rm.entity.monitoring;

import io.kubernetes.client.openapi.models.V1Namespace;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DataObject
public class K8sMonitoringData {
    private List<K8sNode> nodes;

    private List<V1Namespace> namespaces;


    /**
     * Create an instance with a JsonObject.
     *
     * @param jsonObject the JsonObject
     */
    public K8sMonitoringData(JsonObject jsonObject) {
        K8sMonitoringData request = jsonObject.mapTo(K8sMonitoringData.class);
        this.nodes = request.getNodes();
        this.namespaces = request.getNamespaces();
    }

    /**
     * Get the object as a JsonObject.
     *
     * @return the object as JsonObject
     */
    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
