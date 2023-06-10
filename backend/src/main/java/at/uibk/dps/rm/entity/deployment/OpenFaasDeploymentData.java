package at.uibk.dps.rm.entity.deployment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
@Getter
public class OpenFaasDeploymentData {

    private final long deploymentId;
    private final String dockerUserName;
    private final List<Long> resourceIds = new ArrayList<>();
    private final List<String> functionIdentifiers = new ArrayList<>();
    private final List<String> gatewayUrls = new ArrayList<>();
    private long functionCount = 0;

    public void appendValues(long resourceId, String functionIdentifier, String gatewayUrl) {
        this.resourceIds.add(resourceId);
        this.functionIdentifiers.add(functionIdentifier);
        this.gatewayUrls.add(gatewayUrl);
        this.functionCount++;
    }

    private String getOpenFaasString(long resourceId, String functionIdentifier, String gatewayUrl) {
        return String.format(
            "module \"r%s_%s\" {\n" +
                "  openfaas_depends_on = 0\n" +
                "  source = \"../../../terraform/openfaas\"\n" +
                "  name = \"r%s_%s_%s\"\n" +
                "  image = \"%s/%s\"\n" +
                "  basic_auth_user = var.openfaas_login_data[\"r%s\"].auth_user\n" +
                "  vm_props = {\n" +
                "    gateway_url = \"%s\"\n" +
                "    auth_password = var.openfaas_login_data[\"r%s\"].auth_pw\n" +
                "  }\n" +
                "}\n", resourceId, functionIdentifier, resourceId, functionIdentifier, deploymentId, dockerUserName,
            functionIdentifier, resourceId, gatewayUrl, resourceId
        );
    }


    public String getModuleString() {
        if (resourceIds.isEmpty()) {
            return "";
        }
        StringBuilder openFaaS = new StringBuilder();
        for(int i=0; i< functionCount; i++) {
            openFaaS.append(getOpenFaasString(resourceIds.get(i), functionIdentifiers.get(i), gatewayUrls.get(i)));
        }

        return openFaaS.toString();
    }
}
