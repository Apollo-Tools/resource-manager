package at.uibk.dps.rm.testutil.objectprovider;

import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Secret;

import java.util.HashMap;

public class TestK8sProvider {

    public static V1Secret createSecret() {
        V1Secret secret = new V1Secret();
        HashMap<String, byte[]> data = new HashMap<>();
        data.put("cluster1", "secret-value".getBytes());
        data.put("cluster2", "value-secret".getBytes());
        secret.setData(data);
        return secret;
    }

    public static V1Namespace createNamespace(String name) {
        V1Namespace namespace = new V1Namespace();
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(name);
        namespace.setMetadata(metadata);
        return namespace;
    }

    public static V1Node createNode(String name) {
        V1Node node = new V1Node();
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(name);
        node.setMetadata(metadata);
        return node;
    }
}