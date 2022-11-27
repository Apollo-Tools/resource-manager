package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class DeploymentTest {

    public static void main(String[] args) {
        String accessKey = "###";
        String secretKey = "###";
        String sessionToken = "###";
        String region = "us-east-1";
        String awsRole = "LabRole";
        long reservationId = 10;
        ResourceType resourceType = new ResourceType();
        resourceType.setTypeId(1L);
        resourceType.setResourceType("faas");
        Resource resource = new Resource();
        resource.setResourceId(1L);
        resource.setIsSelfManaged(false);
        resource.setResourceType(resourceType);
        MetricValue mv1 = setUpMetricValue(1L, "region", null, "us-east-1", null, "string", resource);
        MetricValue mv2 = setUpMetricValue(2L, "role", null, "LabRole", null, "string", resource);
        MetricValue mv3 = setUpMetricValue(3L, "function-name", null, "template-python", null, "string", resource);
        MetricValue mv4 = setUpMetricValue(4L, "handler", null, "main.lambda_handler", null, "string", resource);
        MetricValue mv5 = setUpMetricValue(5L, "timeout", 300.0, null, null, "number", resource);
        MetricValue mv6 = setUpMetricValue(6L, "memory-size", 256.0, null, null, "number", resource);
        MetricValue mv7 = setUpMetricValue(7L, "layers", null, "", null, "string", resource);
        MetricValue mv8 = setUpMetricValue(8L, "runtime", null, "python3.8", null, "string", resource);
        MetricValue mv9 = setUpMetricValue(9L, "code", null, "", null, "string", resource);
        resource.setMetricValues(List.of(mv1, mv2, mv3, mv4, mv5, mv6, mv7, mv8, mv9));

        List<Resource> resources = List.of(resource);
        try {
            Path terraformFile = Paths.get("temp\\aws\\reservation" + reservationId + ".tf");
            Files.deleteIfExists(terraformFile);
            // Load aws provider
            String loadProvider =
                "terraform {\n" +
                "  required_providers {\n" +
                "    aws = {\n" +
                "      source  = \"hashicorp/aws\"\n" +
                "      version = \"~> 4.16\"\n" +
                "    }\n" +
                "  }\n" +
                "  required_version = \">= 1.2.0\"\n" +
                "}\n";
            // Initialize variables
            String setupVariables =
                "variable \"access_key\" {\n" +
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
            // Setup aws
            String setupProvider = String.format(
                "provider \"aws\" {\n" +
                "  access_key = var.access_key\n" +
                "  secret_key = var.secret_access_key\n" +
                "  token = var.session_token\n" +
                "  region = \"%s\"\n" +
                "}\n", region);
            // Select role
            String setAWSRole = String.format(
                "data \"aws_iam_role\" \"labRole\" {\n" +
                "  name = \"%s\"\n" +
                "}\n", awsRole);
            // Set functionLocals
            StringBuilder functionNames = new StringBuilder();
            StringBuilder functionPaths = new StringBuilder();
            StringBuilder functionRuntimes = new StringBuilder();
            StringBuilder functionTimeouts = new StringBuilder();
            StringBuilder functionMemorySizes = new StringBuilder();
            StringBuilder functionHandlers = new StringBuilder();
            StringBuilder functionLayers = new StringBuilder();
            for (Resource r: resources) {
                List<MetricValue> metricValues = r.getMetricValues();
                functionNames.append("\"").append(metricValues.get(2).getValueString()).append("\",");
                functionPaths.append("\"").append(metricValues.get(2).getValueString()).append(".zip\",");
                functionHandlers.append("\"").append(metricValues.get(3).getValueString()).append("\",");
                functionTimeouts.append(metricValues.get(4).getValueNumber()).append(",");
                functionMemorySizes.append(metricValues.get(5).getValueNumber()).append(",");
                functionLayers.append("[],");
                functionRuntimes.append("\"").append(metricValues.get(7).getValueString()).append("\",");
            }
            String setFunctionLocals = String.format(
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
            // Set functions
            String setFunctions =
                "resource \"aws_lambda_function\" \"lambda\" {\n" +
                    "  count = length(local.function_names)\n" +
                    "  filename      = local.function_paths[count.index]\n" +
                    "  function_name = local.function_names[count.index]\n" +
                    "  role          = data.aws_iam_role.labRole.arn\n" +
                    "  handler       = local.function_handlers[count.index]\n" +
                    "  timeout       = local.function_timeouts[count.index]\n" +
                    "  memory_size   = local.function_memory_sizes[count.index]\n" +
                    "  layers        = local.function_layers[count.index]\n" +
                    "  runtime       = local.function_runtimes[count.index]\n" +
                    "  source_code_hash = filebase64sha256(local.function_paths[count.index])\n" +
                    "}\n";
            // Set function url
            String setFunctionUrl =
                "resource \"aws_lambda_function_url\" \"function_url\" {\n" +
                    "  count = length(local.function_names)\n" +
                    "\n" +
                    "  function_name      = aws_lambda_function.lambda[count.index].function_name\n" +
                    "  authorization_type = \"NONE\"\n" +
                    "}\n";
            // Set output
            String setOutput =
                "output \"function-urls\" {\n" +
                    "  value = aws_lambda_function_url.function_url\n" +
                    "}\n";
            String tfContent = loadProvider + setupVariables + setupProvider + setAWSRole +
                setFunctionLocals + setFunctions + setFunctionUrl + setOutput;
            Files.writeString(terraformFile, tfContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE);

            ProcessBuilder builder = new ProcessBuilder("terraform",  "-chdir=" + terraformFile.getParent(), "init");
            int retValue = executeCli(builder);
            builder = new ProcessBuilder("terraform", "-chdir="  + terraformFile.getParent(),
                "apply", "-auto-approve", "-var=\"access_key=" + accessKey + "\"",
                "-var=\"secret_access_key=" + secretKey + "\"", "-var=\"session_token=" + sessionToken + "\"");
            retValue = executeCli(builder);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static MetricValue setUpMetricValue(long metricValueId, String metric, Double valueDouble, String valueString, Boolean valueBool, String metricType,
                                               Resource resource) {
        MetricValue metricValue = new MetricValue();
        metricValue.setMetricValueId(metricValueId);
        metricValue.setValueNumber(valueDouble);
        metricValue.setValueString(valueString);
        metricValue.setValueBool(valueBool);
        metricValue.setMetric(createMetric(metric, metricType));
        metricValue.setResource(resource);
        metricValue.setCount(10L);
        return metricValue;
    }

    private static Metric createMetric(String name, String metricType) {
        Metric metric = new Metric();
        metric.setMetricId(1L);
        metric.setMetric(name);
        metric.setMetricType(createMetricType(metricType));
        metric.setDescription("Blah");
        metric.setIsMonitored(false);
        return metric;
    }

    public static MetricType createMetricType(String metricTypeName) {
        MetricType metricType = new MetricType();
        metricType.setMetricTypeId(1L);
        metricType.setType(metricTypeName);
        return metricType;
    }

    private static int executeCli(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        processBuilder.redirectErrorStream(true);
        final Process initTF = processBuilder.start();
        Thread thread = printTerraformOutput(initTF);
        thread.start();
        initTF.waitFor();
        initTF.destroy();
        return initTF.exitValue();
    }

    private static Thread printTerraformOutput(Process process) {
        return new Thread(() -> {
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            input.lines().forEach(System.out::println);
        });
    }
}
