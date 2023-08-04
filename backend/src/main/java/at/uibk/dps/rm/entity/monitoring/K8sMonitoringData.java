package at.uibk.dps.rm.entity.monitoring;

import io.kubernetes.client.openapi.models.V1Namespace;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class K8sMonitoringData {
    private final List<K8sNode> nodes;

    private final List<V1Namespace> namespaces;
}
