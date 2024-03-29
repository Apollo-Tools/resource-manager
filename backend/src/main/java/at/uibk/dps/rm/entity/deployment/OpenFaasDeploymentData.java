package at.uibk.dps.rm.entity.deployment;

import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * This class represents all the data for a deployment to OpenFaaS.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
@Getter
public class OpenFaasDeploymentData {

    private final long deploymentId;
    private final DockerCredentials dockerCredentials;
    private final List<Long> resourceIds = new ArrayList<>();
    private final List<String> functionIdentifiers = new ArrayList<>();
    private final List<String> baseUrls = new ArrayList<>();
    private final List<Integer> metricsPorts = new ArrayList<>();
    private final List<Integer> openfaasPorts = new ArrayList<>();
    private final List<Integer> timeouts = new ArrayList<>();
    private long functionCount = 0;

    /**
     * Append new values to the currently stored values.
     *
     * @param resourceId the id of the resource
     * @param functionIdentifier the function identifier
     * @param baseUrl the base url of the resource
     * @param metricsPort the port of the metrics endpoint
     * @param openFaasPort the port of the OpenFaaS gateway
     */
    public void appendValues(long resourceId, String functionIdentifier, String baseUrl, int metricsPort,
            int openFaasPort, int timeout) {
        this.resourceIds.add(resourceId);
        this.functionIdentifiers.add(functionIdentifier);
        this.baseUrls.add(baseUrl);
        this.metricsPorts.add(metricsPort);
        this.openfaasPorts.add(openFaasPort);
        this.timeouts.add(timeout);
        this.functionCount++;
    }

    /**
     * Return the OpenFaaS module definition for a resource deployment.
     *
     * @param resourceId the id of the resource
     * @param functionIdentifier the function identifier
     * @param baseUrl the base url of the resource
     * @param metricsPort the port of the metrics endpoint
     * @param openFaasPort the port of the OpenFaaS gateway
     * @return the OpenFaaS module definition string
     */
    private String getOpenFaasString(long resourceId, String functionIdentifier, String baseUrl, int metricsPort,
            int openFaasPort, int timeout) {
        return String.format(
            "module \"r%s_%s\" {\n" +
                "  openfaas_depends_on = 0\n" +
                "  source = \"../../../terraform/openfaas\"\n" +
                "  deployment_id = %s\n" +
                "  name = \"r%s_%s_%s\"\n" +
                "  image = \"%s/%s\"\n" +
                "  basic_auth_user = var.openfaas_login_data[\"r%s\"].auth_user\n" +
                "  vm_props = {\n" +
                "    base_url = \"%s\"\n" +
                "    metrics_port = %s\n" +
                "    openfaas_port = %s\n" +
                "    auth_password = var.openfaas_login_data[\"r%s\"].auth_pw\n" +
                "  }\n" +
                "  timeout = %s\n" +
                "}\n", resourceId, functionIdentifier, deploymentId, resourceId, functionIdentifier, deploymentId,
            dockerCredentials.getUsername(), functionIdentifier, resourceId, baseUrl, metricsPort, openFaasPort,
            resourceId, timeout
        );
    }

    /**
     * Get the all module definitions composed by the stored values.
     *
     * @return the module definitions
     */
    public String getModuleString() {
        if (resourceIds.isEmpty()) {
            return "";
        }
        StringBuilder openFaaS = new StringBuilder();
        for(int i=0; i< functionCount; i++) {
            openFaaS.append(getOpenFaasString(resourceIds.get(i), functionIdentifiers.get(i), baseUrls.get(i),
                metricsPorts.get(i), openfaasPorts.get(i), timeouts.get(i)));
        }

        return openFaaS.toString();
    }
}
