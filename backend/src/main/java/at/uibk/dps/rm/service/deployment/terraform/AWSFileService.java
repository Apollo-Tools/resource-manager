package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.service.deployment.TerraformModule;
import at.uibk.dps.rm.service.deployment.sourcecode.PackagePythonCode;
import at.uibk.dps.rm.service.deployment.sourcecode.PackageSourceCode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class AWSFileService extends ModuleFileService {

    private final String region;
    private final String awsRole;
    private final List<FunctionResource> functionResources;
    private final long reservationId;

    private final TerraformModule module;

    private final Map<String, String> defaultValues = setDefaultValues();

    private final Set<Long> faasFunctionIds = new HashSet<>();

    private final Set<Long> vmResourceIds = new HashSet<>();
    private final Set<Long> vmFunctionIds = new HashSet<>();

    public AWSFileService(Path rootFolder, String region, String awsRole, List<FunctionResource> functionResources,
                          long reservationId, TerraformModule module) {
        super(rootFolder);
        this.region = region;
        this.awsRole = awsRole;
        this.functionResources = functionResources;
        this.reservationId = reservationId;
        this.module = module;
    }


    @Override
    protected String getProviderString() {
        return String.format(
            "provider \"aws\" {\n" +
                "  access_key = var.access_key\n" +
                "  secret_key = var.secret_access_key\n" +
                "  token = var.session_token\n" +
                "  region = \"%s\"\n" +
                "}\n", region);
    }

    // TODO: rework access to metric values
    @Override
    protected String getFunctionsModulString(List<FunctionResource> functionResources, long reservationId,
                                             Path rootFolder) throws IOException {
        StringBuilder functionNames = new StringBuilder(), functionPaths = new StringBuilder(),
            functionRuntimes = new StringBuilder(), functionTimeouts = new StringBuilder(),
            functionMemorySizes = new StringBuilder(), functionHandlers = new StringBuilder(),
            functionLayers = new StringBuilder();
        for (FunctionResource fr: functionResources) {
            Resource resource = fr.getResource();
            if (!resource.getResourceType().getResourceType().equals("faas")) {
                continue;
            }
            Function function = fr.getFunction();
            PackageSourceCode packageSourceCode;
            String runtime = function.getRuntime().getName();
            String functionIdentifier =  function.getName() +
                "_" + runtime.replace(".", "") +
                "_" + reservationId;
            functionNames.append("\"").append("r").append(resource.getResourceId())
                .append("_").append(functionIdentifier)
                .append("\",");
            functionPaths.append("\"")
                .append(rootFolder.toAbsolutePath().toString().replace("\\","/")).append("/")
                .append(functionIdentifier)
                .append(".zip\",");
            if (runtime.startsWith("python")) {
                functionHandlers.append("\"main.handler\",");
                if (!faasFunctionIds.contains(function.getFunctionId())) {
                    packageSourceCode = new PackagePythonCode();
                    packageSourceCode.composeSourceCode(rootFolder, functionIdentifier,
                        function.getCode());
                    faasFunctionIds.add(function.getFunctionId());
                }
            }
            Map<String, MetricValue> metricValues = resource.getMetricValues()
                .stream()
                .collect(Collectors.toMap(metricValue -> metricValue.getMetric().getMetric(),
                    metricValue -> metricValue));
            functionTimeouts.append(metricValues.containsKey("timeout") ? metricValues.get("timeout").getValueNumber() :
                                    defaultValues.get("timeout")).append(",");
            functionMemorySizes.append(metricValues.containsKey("memory-size") ? metricValues.get("memory-size")
                                        .getValueNumber() : defaultValues.get("memory-size")).append(",");
            functionLayers.append("[],");
            functionRuntimes.append("\"").append(runtime).append("\",");
        }
        if (faasFunctionIds.isEmpty()) {
            return "";
        }

        return String.format(
            "module \"faas\" {\n" +
                "  source = \"../../../terraform/aws/faas\"\n" +
                "  names = [%s]\n" +
                "  paths = [%s]\n" +
                "  handlers = [%s]\n" +
                "  timeouts = [%s]\n" +
                "  memory_sizes = [%s]\n" +
                "  layers = [%s]\n" +
                "  runtimes = [%s]\n" +
                "  aws_role = \"%s\"\n" +
                "}\n", functionNames, functionPaths, functionHandlers, functionTimeouts,
            functionMemorySizes, functionLayers, functionRuntimes, awsRole
        );
    }

    private boolean checkMustDeployVM(Resource resource) {
        return resource.getResourceType().getResourceType().equals("vm") &&
            !resource.getIsSelfManaged() && !vmResourceIds.contains(resource.getResourceId());
    }

    @Override
    protected String getVmModulesString(List<FunctionResource> functionResources) {
        StringBuilder resourceNamesString = new StringBuilder(),
            instanceTypesString = new StringBuilder();

        for (FunctionResource functionResource: functionResources) {
            Resource resource = functionResource.getResource();
            Function function = functionResource.getFunction();
            if (checkMustDeployVM(resource)) {
                resourceNamesString.append("\"resource_").append(resource.getResourceId()).append("\",");
                instanceTypesString.append("\"").append(defaultValues.get("instance-type")).append("\",");
                vmResourceIds.add(resource.getResourceId());
            }
            // TODO: push function onto vm
        }

        if (vmResourceIds.isEmpty()) {
            return "";
        }

        // TODO: get vpc from persisted values
        return String.format(
            "module \"vm\" {\n" +
                "  source         = \"../../../terraform/aws/vm\"\n" +
                "  reservation    = \"%s\"\n" +
                "  names          = [%s]\n" +
                "  instance_types = [%s]\n" +
                "  vpc_id         = \"%s\"\n" +
                "  subnet_id      = \"%s\"\n" +
                "}", reservationId, resourceNamesString, instanceTypesString, defaultValues.get("vpc-id"),
            defaultValues.get("subnet-id")
        );
    }

    // TODO: Enforce different resource types to have specific properties set (e.g. code, function-type, region)
    // TODO: Persist default values
    private Map<String, String> setDefaultValues() {
        Map<String, String> defaultValues = new HashMap<>();
        defaultValues.put("awsrole", "LabRole");
        defaultValues.put("timeout", "300.0");
        defaultValues.put("memory-size", "256.0");
        defaultValues.put("layers", "");
        defaultValues.put("vpc-id", "vpc-03e37d94124ae821c");
        defaultValues.put("subnet-id", "subnet-02109321bd7f82080");
        defaultValues.put("instance-type", "t2.micro");
        return defaultValues;
    }

    @Override
    protected String getCredentialVariablesString() {
        return "variable \"access_key\" {\n" +
            "  type = string\n" +
            "  default = \"\"\n" +
            "}\n" +
            "variable \"secret_access_key\" {\n" +
            "  type = string\n" +
            "  default = \"\"\n" +
            "}\n" +
            "variable \"session_token\" {\n" +
            "  type = string\n" +
            "  default = \"\"\n" +
            "}\n";
    }

    // TODO: rework for vms
    @Override
    protected String getOutputString() {
        StringBuilder outputString = new StringBuilder();
        if (!this.faasFunctionIds.isEmpty()) {
            String functionUrl =
                "output \"function_urls\" {\n" +
                "  value = module.faas.function_urls\n" +
                "}\n";
            outputString.append(functionUrl);
        }
        if (!this.vmResourceIds.isEmpty()) {
            String vmProps =
                "output \"vm_props\" {\n" +
                "  value = module.vm.vm_props\n" +
                "  sensitive = true\n" +
                "}\n";
            outputString.append(vmProps);
        }
        setModuleGlobalOutputString();
        return outputString.toString();
    }

    @Override
    protected void setModuleGlobalOutputString() {
        StringBuilder outputString = new StringBuilder();
        if (!this.faasFunctionIds.isEmpty()) {
            String functionUrl = String.format("output \"%s_function_urls\" {\n" +
                "  value = module.%s.function_urls\n" +
                "}\n", module.getModuleName(), module.getModuleName());
            outputString.append(functionUrl);
        }
        if (!this.vmResourceIds.isEmpty()) {
            String vmProps = String.format("output \"%s_vm_props\" {\n" +
                "  value = module.%s.vm_props\n" +
                "  sensitive = true\n" +
                "}\n", module.getModuleName(), module.getModuleName());
            outputString.append(vmProps);
        }
        this.module.setGlobalOutput(outputString.toString());
    }

    @Override
    protected String getMainFileContent() throws IOException {
        return this.getProviderString() +
            this.getFunctionsModulString(functionResources, reservationId, getRootFolder()) +
            this.getVmModulesString(functionResources);
    }

    @Override
    protected String getVariablesFileContent() {
        return this.getCredentialVariablesString();
    }

    @Override
    protected String getOutputsFileContent() {
        return this.getOutputString();
    }
}
