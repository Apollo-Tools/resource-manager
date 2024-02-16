package at.uibk.dps.rm.testutil.mockprovider;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import io.kubernetes.client.Metrics;
import io.kubernetes.client.custom.NodeMetricsList;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1SecretList;
import lombok.experimental.UtilityClass;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import static org.mockito.BDDMockito.given;

/**
 * Utility class to mock (mocked construction) k8s objects for tests.
 *
 * @author matthi-g
 */
@UtilityClass
public class K8sObjectMockprovider {
    public static MockedConstruction<CoreV1Api> mockCoreV1ApiListNamedSecrets(ConfigDTO config,
                                                                              V1SecretList secretList) {
        return Mockito.mockConstruction(CoreV1Api.class, (mock, context) ->
            given(mock.listNamespacedSecret(config.getKubeConfigSecretsNamespace(), null,
            null, null, "metadata.name=" +
                config.getKubeConfigSecretsName(), null, null, null,
                null, config.getKubeApiTimeoutSeconds(), null))
            .willReturn(secretList));
    }

    public static MockedConstruction<CoreV1Api> mockCoreV1ApiListNamedSecretsException(ConfigDTO config) {
        return Mockito.mockConstruction(CoreV1Api.class, (mock, context) ->
            given(mock.listNamespacedSecret(config.getKubeConfigSecretsNamespace(), null,
            null, null, "metadata.name=" +
                config.getKubeConfigSecretsName(), null, null, null,
                null, config.getKubeApiTimeoutSeconds(), null))
            .willThrow(ApiException.class));
    }

    public static MockedConstruction<CoreV1Api> mockCoreV1ApiListNamespaces(ConfigDTO config,
                                                                            V1NamespaceList namespaceList) {
        return Mockito.mockConstruction(CoreV1Api.class, (mock, context) ->
            given(mock.listNamespace(null, null,  null, null,
                null, null, null, null,
                config.getKubeApiTimeoutSeconds(), null))
            .willReturn(namespaceList));
    }

    public static MockedConstruction<CoreV1Api> mockCoreV1ApiListNamespacesException(ConfigDTO config) {
        return Mockito.mockConstruction(CoreV1Api.class, (mock, context) ->
            given(mock.listNamespace(null, null,  null, null,
                null, null, null, null,
                config.getKubeApiTimeoutSeconds(), null))
            .willThrow(ApiException.class));
    }

    public static MockedConstruction<CoreV1Api> mockCoreV1ApiListNodes(ConfigDTO config,
                                                                       V1NodeList nodeList) {
        return Mockito.mockConstruction(CoreV1Api.class, (mock, context) ->
            given(mock.listNode(null, null,  null, null,
                null, null, null, null, config.getKubeApiTimeoutSeconds(),
                false))
            .willReturn(nodeList));
    }

    public static MockedConstruction<CoreV1Api> mockCoreV1ApiListNodesException(ConfigDTO config) {
        return Mockito.mockConstruction(CoreV1Api.class, (mock, context) ->
            given(mock.listNode(null, null,  null, null,
                null, null, null, null, config.getKubeApiTimeoutSeconds(),
                false))
            .willThrow(ApiException.class));
    }

    public static MockedConstruction<Metrics> mockMetricsNodeUtilisation(NodeMetricsList nodeMetricsList) {
        return Mockito.mockConstruction(Metrics.class, (mock, context) ->
            given(mock.getNodeMetrics()).willReturn(nodeMetricsList));
    }
}
