package at.uibk.dps.rm.service.monitoring.k8s;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.K8sNode;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Namespace;

import java.util.List;
import java.util.Map;

public interface K8sMonitoringService {
    Map<String, String> listSecrets(ConfigDTO config);

    List<V1Namespace> listNamespaces(String kubeConfig, ConfigDTO config) throws ApiException;

    List<K8sNode> listNodes(String kubeConfig, ConfigDTO config) throws ApiException;

    void getCurrentNodeAllocation(K8sNode node, String kubeConfig, ConfigDTO config);
}
