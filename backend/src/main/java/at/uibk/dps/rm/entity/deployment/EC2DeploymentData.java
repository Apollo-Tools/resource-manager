package at.uibk.dps.rm.entity.deployment;

import at.uibk.dps.rm.entity.model.VPC;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class represents all the data for a deployment to AWS EC2.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
@Getter
public class EC2DeploymentData {

    private final long deploymentId;
    private final VPC vpc;
    private final String dockerUserName;
    private final List<Long> resourceIds = new ArrayList<>();
    private final List<String> functionIdentifiers = new ArrayList<>();
    private final List<String> resourceNames = new ArrayList<>();
    private final Map<String, String> instanceTypeMapping = new HashMap<>();
    private long functionCount = 0;

    /**
     * Append new values to the currently stored values.
     *
     * @param resourceName the name of the resource
     * @param instanceType the instance type
     * @param resourceId the id of the resource
     * @param functionIdentifier the function identifier
     */
    public void appendValues(String resourceName, String instanceType, long resourceId, String functionIdentifier) {
        if (!instanceTypeMapping.containsKey(resourceName)) {
            this.instanceTypeMapping.put(resourceName, instanceType);
        }
        appendValues(resourceName, resourceId, functionIdentifier);
    }

    /**
     * Append new values to the currently stored values.
     *
     * @param resourceName the name of the resource
     * @param resourceId the id of the resource
     * @param functionIdentifier the function identifier
     */
    public void appendValues(String resourceName, long resourceId, String functionIdentifier) {
        this.resourceNames.add(resourceName);
        this.resourceIds.add(resourceId);
        this.functionIdentifiers.add(functionIdentifier);
        this.functionCount++;
    }

    /**
     * Add quotes to a string value.
     *
     * @param value the string value
     * @return the quoted string
     */
    private String addQuotes(String value) {
        return "\"" + value + "\",";
    }

    /**
     * Return the OpenFaaS module definition for a resource deployment.
     *
     * @param resourceId the id of the resource
     * @param functionIdentifier the function identifier
     * @param resourceName the name of the resource
     * @return the OpenFaaS module definition string
     */
    private String getOpenFaasString(long resourceId, String functionIdentifier, String resourceName) {
        return String.format(
            "module \"r%s_%s\" {\n" +
                "  openfaas_depends_on = module.ec2\n" +
                "  source = \"../../../terraform/openfaas\"\n" +
                "  name = \"r%s_%s_%s\"\n" +
                "  deployment_id = %s\n" +
                "  image = \"%s/%s\"\n" +
                "  basic_auth_user = \"admin\"\n" +
                "  vm_props = module.ec2.vm_props[\"%s\"]\n" +
                "}\n", resourceId, functionIdentifier, resourceId, functionIdentifier, deploymentId, deploymentId,
            dockerUserName, functionIdentifier, resourceName
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

        String names = instanceTypeMapping.keySet().stream().map(this::addQuotes).collect(Collectors.joining());
        String instanceTypes = instanceTypeMapping.values().stream().map(this::addQuotes).collect(Collectors.joining());
        StringBuilder openFaaS = new StringBuilder();
        for(int i=0; i< functionCount; i++) {
            openFaaS.append(getOpenFaasString(resourceIds.get(i), functionIdentifiers.get(i), resourceNames.get(i)));
        }

        return String.format(
            "module \"ec2\" {\n" +
                "  source         = \"../../../terraform/aws/vm\"\n" +
                "  deployment_id  = %s\n" +
                "  names          = [%s]\n" +
                "  instance_types = [%s]\n" +
                "  vpc_id         = \"%s\"\n" +
                "  subnet_id      = \"%s\"\n" +
                "}\n", deploymentId, names, instanceTypes, vpc.getVpcIdValue(),
            vpc.getSubnetIdValue()) + openFaaS;
    }
}
