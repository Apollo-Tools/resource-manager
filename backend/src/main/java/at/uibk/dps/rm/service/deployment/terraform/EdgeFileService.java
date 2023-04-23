package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.util.MetricValueMapper;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.util.*;

/**
 * Extension of the #TerraformFileService to set up the edge module of a deployment.
 *
 * @author matthi-g
 */
public class EdgeFileService extends TerraformFileService {
    private final List<FunctionResource> functionResources;

    private final long reservationId;

    private final Set<Long> edgeFunctionIds = new HashSet<>();

    private final String dockerUserName;

    /**
     * Create an instance from the fileSystem, rootFolder, functionResources, reservationId and
     * dockerUsername.
     *
     * @param fileSystem the vertx file system
     * @param rootFolder the root folder of the module
     * @param functionResources the list of function resources
     * @param reservationId the id of the reservation
     * @param dockerUsername the docker username
     */
    public EdgeFileService(FileSystem fileSystem, Path rootFolder, List<FunctionResource> functionResources,
                           long reservationId, String dockerUsername) {
        super(fileSystem, rootFolder);
        this.functionResources = functionResources;
        this.reservationId = reservationId;
        this.dockerUserName = dockerUsername;
    }

    @Override
    protected String getProviderString() {
        return "";
    }

    @Override
    protected String getMainFileContent() {
        return this.getEdgeModulesString();
    }

    /**
     * Get the string that defines all edge resource from the terraform module.
     *
     * @return the edge modules string
     */
    private String getEdgeModulesString() {
        StringBuilder functionsString = new StringBuilder();
        for (FunctionResource functionResource : functionResources) {
            Resource resource = functionResource.getResource();
            Function function = functionResource.getFunction();
            if (!resource.getResourceType().getResourceType().equals("edge")) {
                continue;
            }
            Map<String, MetricValue> metricValues = MetricValueMapper.mapMetricValues(resource.getMetricValues());
            String functionIdentifier =  function.getFunctionDeploymentId();
            functionsString.append(String.format(
                "module \"r%s_%s\" {\n" +
                    "  openfaas_depends_on = 0\n" +
                    "  source = \"../../../terraform/openfaas\"\n" +
                    "  name = \"r%s_%s_%s\"\n" +
                    "  image = \"%s/%s\"\n" +
                    "  basic_auth_user = var.login_data[%s].auth_user\n" +
                    "  vm_props = {\n" +
                    "    gateway_url = \"%s\"\n" +
                    "    auth_password = var.login_data[%s].auth_pw\n" +
                    "  }\n" +
                    "}\n", resource.getResourceId(), functionIdentifier, resource.getResourceId(),
                functionIdentifier, reservationId, dockerUserName, functionIdentifier, edgeFunctionIds.size(),
                metricValues.get("gateway-url").getValueString(), edgeFunctionIds.size()
            ));
            edgeFunctionIds.add(function.getFunctionId());
        }
        return functionsString.toString();
    }

    @Override
    protected String getCredentialVariablesString() {
        return "";
    }

    @Override
    protected String getVariablesFileContent() {
        return "variable \"login_data\" {\n" +
            "  type = list(object({\n" +
            "    auth_user = string\n" +
            "    auth_pw = string\n" +
            "  }))\n" +
            "}";
    }

    @Override
    protected String getOutputString() {
        StringBuilder functionNames = new StringBuilder(), functionModuleOutputs = new StringBuilder();
        int edgeCount = 0;
        for (FunctionResource functionResource : functionResources) {
            Resource resource = functionResource.getResource();
            Function function = functionResource.getFunction();
            if (!resource.getResourceType().getResourceType().equals("edge")) {
                continue;
            }
            edgeCount ++;
            String functionIdentifier = "r" + resource.getResourceId() + "_" + function.getFunctionDeploymentId();
            functionNames.append(String.format("\"%s\",", functionIdentifier));
            functionModuleOutputs.append(String.format("module.%s.function_url,", functionIdentifier));
        }
        if (edgeCount == 0) {
            return "";
        }

        return String.format("output \"edge_urls\" {\n" +
            "  value = zipmap([%s], [%s])\n" +
            "}\n", functionNames, functionModuleOutputs);
    }

    @Override
    protected String getOutputsFileContent() {
        return this.getOutputString();
    }
}
