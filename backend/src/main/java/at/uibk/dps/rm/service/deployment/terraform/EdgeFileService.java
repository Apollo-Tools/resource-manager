package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.Resource;

import java.nio.file.Path;
import java.util.*;

public class EdgeFileService extends TerraformFileService {
    private final List<FunctionResource> functionResources;

    private final long reservationId;

    private final Map<String, String> defaultValues = new HashMap<>();

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
            String runtime = function.getRuntime().getName().toLowerCase();
            String functionIdentifier =  function.getName().toLowerCase() +
                "_" + runtime.replace(".", "");
            functionsString.append(String.format(
                "module \"r%s_%s\" {\n" +
                    "  openfaas_depends_on = 0\n" +
                    "  source = \"../../../terraform/openfaas\"\n" +
                    "  name = \"r%s_%s_%s\"\n" +
                    "  image = \"%s/%s\"\n" +
                    "  basic_auth_user = \"admin\"\n" +
                    "  vm_props = {\n" +
                    "    gateway_url = \"http://192.168.10.131:8080\"\n" +
                    "    auth_password = \"123\"\n" +
                    "  }\n" +
                    "}\n", resource.getResourceId(), functionIdentifier, resource.getResourceId(),
                functionIdentifier, reservationId, dockerUserName, functionIdentifier, "resourceName"
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
        return "";
    }

    @Override
    protected String getOutputString() {
        return "";
    }

    @Override
    protected String getOutputsFileContent() {
        return "";
    }
}
