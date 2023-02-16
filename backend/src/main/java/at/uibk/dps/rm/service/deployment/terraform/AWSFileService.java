package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.*;
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

    private final Set<Long> faasFunctionIds = new HashSet<>();

    public AWSFileService(Path rootFolder, String region, String awsRole, List<FunctionResource> functionResources,
                          long reservationId) {
        super(rootFolder);
        this.region = region;
        this.awsRole = awsRole;
        this.functionResources = functionResources;
        this.reservationId = reservationId;
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

    @Override
    protected String getRoleString(String roleName) {
        return String.format(
            "data \"aws_iam_role\" \"awsRole\" {\n" +
                "  name = \"%s\"\n" +
                "}\n", roleName);
    }

    // TODO: rework access to metric values
    @Override
    protected String getFunctionLocalsString(List<FunctionResource> functionResources, long reservationId,
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
            functionPaths.append("\"").append(functionIdentifier).append(".zip\",");
            if (runtime.startsWith("python")) {
                functionHandlers.append("\"main.handler\",");
                if (!faasFunctionIds.contains(function.getFunctionId())) {
                    packageSourceCode = new PackagePythonCode();
                    packageSourceCode.composeSourceCode(rootFolder, functionIdentifier,
                        function.getCode());
                    faasFunctionIds.add(function.getFunctionId());
                }
            }
            Map<String, String> defaultValues = setDefaultValues();
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
            "locals {\n" +
                "  function_names = [%s]\n" +
                "  function_paths = [%s]\n" +
                "  function_handlers = [%s]\n" +
                "  function_timeouts = [%s]\n" +
                "  function_memory_sizes = [%s]\n" +
                "  function_layers = [%s]\n" +
                "  function_runtimes = [%s]\n" +
                "}\n", functionNames, functionPaths, functionHandlers, functionTimeouts,
            functionMemorySizes, functionLayers, functionRuntimes
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
        return defaultValues;
    }

    @Override
    protected String getFunctionsString() {
        if (faasFunctionIds.isEmpty()) {
            return "";
        }

        return "resource \"aws_lambda_function\" \"lambda\" {\n" +
            "  count = length(local.function_names)\n" +
            "  filename      = \"${path.module}/${local.function_paths[count.index]}\"\n" +
            "  function_name = local.function_names[count.index]\n" +
            "  role          = data.aws_iam_role.awsRole.arn\n" +
            "  handler       = local.function_handlers[count.index]\n" +
            "  timeout       = local.function_timeouts[count.index]\n" +
            "  memory_size   = local.function_memory_sizes[count.index]\n" +
            "  layers        = local.function_layers[count.index]\n" +
            "  runtime       = local.function_runtimes[count.index]\n" +
            "  source_code_hash = filebase64sha256(\"${path.module}/${local.function_paths[count.index]}\")\n" +
            "}\n";
    }

    @Override
    protected String getFunctionUrlString() {
        if (faasFunctionIds.isEmpty()) {
            return "";
        }

        return "resource \"aws_lambda_function_url\" \"function_url\" {\n" +
            "  count = length(local.function_names)\n" +
            "  function_name      = aws_lambda_function.lambda[count.index].function_name\n" +
            "  authorization_type = \"NONE\"\n" +
            "}\n";
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
        String module = "aws_lambda_function_url";
        return String.format("output \"function_url\" {\n" +
            "  value = %s.function_url\n" +
            "}\n", module);
    }

    @Override
    protected String getMainFileContent() throws IOException {
        return this.getProviderString() + this.getRoleString(awsRole) +
            this.getFunctionLocalsString(functionResources, reservationId, getRootFolder()) + this.getFunctionsString() +
            this.getFunctionUrlString();
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
