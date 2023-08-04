package at.uibk.dps.rm.entity.monitoring;

import io.kubernetes.client.openapi.models.V1Node;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@RequiredArgsConstructor
@Getter
@Setter
public class K8sNode {

    private final V1Node node;

    private int cpuLoad;

    private int memoryLoad;

    private long storageLoad;

    public String getName() {
        return Objects.requireNonNull(node.getMetadata()).getName();
    }
}
