package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.service.deployment.sourcecode.PackagePythonCode;
import at.uibk.dps.rm.service.deployment.sourcecode.PackageSourceCode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AWSFileService extends ModuleFileService {

    private final String region;
    private final String awsRole;
    private final List<Resource> resources;
    private final long reservationId;

    public AWSFileService(Path rootFolder, String region, String awsRole, List<Resource> resources, long reservationId) {
        super(rootFolder);
        this.region = region;
        this.awsRole = awsRole;
        this.resources = resources;
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
    protected String getFunctionLocalsString(List<Resource> resources, long reservationId, Path rootFolder) throws IOException {
        StringBuilder functionNames = new StringBuilder();
        StringBuilder functionPaths = new StringBuilder();
        StringBuilder functionRuntimes = new StringBuilder();
        StringBuilder functionTimeouts = new StringBuilder();
        StringBuilder functionMemorySizes = new StringBuilder();
        StringBuilder functionHandlers = new StringBuilder();
        StringBuilder functionLayers = new StringBuilder();
        for (Resource r: resources) {
            PackageSourceCode packageSourceCode;
            Map<String, String> defaultValues = setDefaultValues(r);
            List<MetricValue> metricValues = r.getMetricValues();
            String runtime = defaultValues.get("runtime").toLowerCase();
            String functionIdentifier = defaultValues.get("function-type") + "_" +
                runtime.replace(".", "") + "_" + reservationId;
            functionNames.append("\"").append(functionIdentifier).append("\",");
            functionPaths.append("\"").append(functionIdentifier).append(".zip\",");
            if (runtime.startsWith("python")) {
                functionHandlers.append("\"main.handler\",");
                packageSourceCode = new PackagePythonCode();
                packageSourceCode.composeSourceCode(rootFolder, functionIdentifier,
                    defaultValues.get("code"));
            }
            functionTimeouts.append(defaultValues.get("timeout")).append(",");
            functionMemorySizes.append(defaultValues.get("memory-size")).append(",");
            functionLayers.append("[],");
            functionRuntimes.append("\"").append(runtime).append("\",");
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




    // TODO: Enforce different resource types to have specifice properties set (e.g. code, function-type, region)
    // TODO: Persist default values and link to user
    private Map<String, String> setDefaultValues(Resource resource) {
        Map<String, String> defaultValues = new HashMap<>();
        defaultValues.put("region", "us-east-1");
        defaultValues.put("awsrole", "LabRole");
        defaultValues.put("function-type", "count");
        defaultValues.put("timeout", "300.0");
        defaultValues.put("memory-size", "256.0");
        defaultValues.put("layers", "");
        defaultValues.put("runtime", "python3.8");
        defaultValues.put("code", "def main(json_input):\n" +
            "    input1 = json_input[\"input1\"]\n    # Processing\n    # return the result\n    res = {\n" +
            "        \"input1\": input1\n    }\n    return res\n");
        return defaultValues;
    }

    @Override
    protected String getFunctionsString() {
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
            this.getFunctionLocalsString(resources, reservationId, getRootFolder()) + this.getFunctionsString() +
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
