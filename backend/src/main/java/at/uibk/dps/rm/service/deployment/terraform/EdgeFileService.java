package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class EdgeFileService extends TerraformFileService {
    private final List<FunctionResource> functionResources;

    private final long reservationId;

    private final Set<Long> edgeFunctionIds = new HashSet<>();

    private final String dockerUserName;

    public EdgeFileService(Path rootFolder, List<FunctionResource> functionResources,
                           long reservationId, String dockerUsername) {
        super(rootFolder);
        this.functionResources = functionResources;
        this.reservationId = reservationId;
        this.dockerUserName = dockerUsername;
    }

    @Override
    protected String getProviderString() {
        return null;
    }

    @Override
    protected String getMainFileContent() {
        return this.getEdgeModulesString();
    }

    private String getEdgeModulesString() {
        StringBuilder functionsString = new StringBuilder();
        for (FunctionResource functionResource : functionResources) {
            Resource resource = functionResource.getResource();
            Function function = functionResource.getFunction();
            if (!resource.getResourceType().getResourceType().equals("edge")) {
                continue;
            }
            Map<String, MetricValue> metricValues = resource.getMetricValues()
                .stream()
                .collect(Collectors.toMap(metricValue -> metricValue.getMetric().getMetric(),
                    metricValue -> metricValue));
            String runtime = function.getRuntime().getName().toLowerCase();
            String functionIdentifier =  function.getName().toLowerCase() +
                "_" + runtime.replace(".", "");
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
        return null;
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
        for (FunctionResource functionResource : functionResources) {
            Resource resource = functionResource.getResource();
            Function function = functionResource.getFunction();
            if (!resource.getResourceType().getResourceType().equals("edge")) {
                continue;
            }
            String runtime = function.getRuntime().getName().toLowerCase();
            String functionIdentifier = "r" + resource.getResourceId() + "_" + function.getName().toLowerCase() +
                "_" + runtime.replace(".", "");
            functionNames.append(String.format("\"%s\",", functionIdentifier));
            functionModuleOutputs.append(String.format("\"module.%s.function_url\",", functionIdentifier));
        }
        return String.format("output \"edge_urls\" {\n" +
            "  value = zipmap([%s], [%s])\n" +
            "}", functionNames, functionModuleOutputs);
    }

    @Override
    protected String getOutputsFileContent() {
        return this.getOutputString();
    }
}
