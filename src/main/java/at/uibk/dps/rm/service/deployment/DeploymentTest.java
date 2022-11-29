package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DeploymentTest {

    public static void main(String[] args) {
        String accessKey = "###";
        String secretKey = "###";
        String sessionToken = "###";
        String region = "us-east-1";
        String awsRole = "LabRole";
        long reservationId = 11;
        ResourceType resourceType = new ResourceType();
        resourceType.setTypeId(1L);
        resourceType.setResourceType("faas");
        Resource resource = new Resource();
        resource.setResourceId(1L);
        resource.setIsSelfManaged(false);
        resource.setResourceType(resourceType);
        MetricValue mv1 = setUpMetricValue(1L, "region", null, "us-east-1", null, "string", resource);
        MetricValue mv2 = setUpMetricValue(2L, "role", null, "LabRole", null, "string", resource);
        MetricValue mv3 = setUpMetricValue(3L, "function-type", null, "count", null, "string", resource);
        MetricValue mv4 = setUpMetricValue(4L, "timeout", 300.0, null, null, "number", resource);
        MetricValue mv5 = setUpMetricValue(5L, "memory-size", 256.0, null, null, "number", resource);
        MetricValue mv6 = setUpMetricValue(6L, "layers", null, "", null, "string", resource);
        MetricValue mv7 = setUpMetricValue(7L, "runtime", null, "python3.8", null, "string", resource);
        MetricValue mv8 = setUpMetricValue(8L, "code", null, "def main(json_input):\n" +
            "    input1 = json_input[\"input1\"]\n    # Processing\n    # return the result\n    res = {\n" +
            "        \"input1\": input1\n    }\n    return res\n", null, "string", resource);
        resource.setMetricValues(List.of(mv1, mv2, mv3, mv4, mv5, mv6, mv7, mv8));

        List<Resource> resources = List.of(resource);
        try {
            Path rootFolder = Paths.get("temp\\reservation_" + reservationId);
            Path terraformFile = Paths.get(rootFolder + "\\aws-" + region + "\\deploy.tf");
            Files.deleteIfExists(terraformFile);
            // Load aws provider
            String loadProvider = getProviderString();
            // Initialize variables
            String setupVariables = getAWSCredentialVariables();
            // Setup aws
            String setupProvider = getAWSProvider(region);
            // Select role
            String setAWSRole = getAWSRole(awsRole);
            // Set functionLocals
            String setFunctionLocals = setFunctionLocals(resources, reservationId, terraformFile);
                // Set functions
            String setFunctions = setFunctions();
            // Set function url
            String setFunctionUrl = setFunctionUrl();
            // Set output
            String setOutput = setOutput();
            String tfContent = setupVariables + setupProvider + setAWSRole +
                setFunctionLocals + setFunctions + setFunctionUrl + setOutput;
            Files.createDirectories(rootFolder);
            Files.createDirectories(terraformFile.getParent());
            // TODO: setup all files
            Path mainFile = Paths.get(rootFolder + "\\main.tf");
            Path variableFile = Paths.get(rootFolder + "\\variables.tf");
            Files.deleteIfExists(terraformFile);

            Files.writeString(terraformFile, tfContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE);

            ProcessBuilder builder = new ProcessBuilder("terraform",  "-chdir=" + terraformFile.getParent(), "init");
            System.out.println("Return value: " + executeCli(builder));
            builder = new ProcessBuilder("terraform", "-chdir="  + terraformFile.getParent(),
                "apply", "-auto-approve", "-var=\"access_key=" + accessKey + "\"",
                "-var=\"secret_access_key=" + secretKey + "\"", "-var=\"session_token=" + sessionToken + "\"");
            System.out.println("Return value: " + executeCli(builder));
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

    private static String getProviderString() {
        return "terraform {\n" +
            "  required_providers {\n" +
            "    aws = {\n" +
            "      source  = \"hashicorp/aws\"\n" +
            "      version = \"~> 4.16\"\n" +
            "    }\n" +
            "  }\n" +
            "  required_version = \">= 1.2.0\"\n" +
            "}\n";
    }

    private static String getAWSCredentialVariables() {
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

    private static String getAWSProvider(String region) {
        return String.format(
            "provider \"aws\" {\n" +
            "  access_key = var.access_key\n" +
            "  secret_key = var.secret_access_key\n" +
            "  token = var.session_token\n" +
            "  region = \"%s\"\n" +
            "}\n", region);
    }

    private static String getAWSRole(String awsRole) {
        return String.format(
            "data \"aws_iam_role\" \"labRole\" {\n" +
            "  name = \"%s\"\n" +
            "}\n", awsRole);
    }

    private static String setFunctionLocals(List<Resource> resources, long reservationId, Path terraformFile) throws IOException {
        StringBuilder functionNames = new StringBuilder();
        StringBuilder functionPaths = new StringBuilder();
        StringBuilder functionRuntimes = new StringBuilder();
        StringBuilder functionTimeouts = new StringBuilder();
        StringBuilder functionMemorySizes = new StringBuilder();
        StringBuilder functionHandlers = new StringBuilder();
        StringBuilder functionLayers = new StringBuilder();
        for (Resource r: resources) {
            List<MetricValue> metricValues = r.getMetricValues();
            String runtime = metricValues.get(6).getValueString().toLowerCase();
            String functionIdentifier = metricValues.get(2).getValueString() + "_" +
                runtime.replace(".", "") + "_" + reservationId;
            functionNames.append("\"").append(functionIdentifier).append("\",");
            functionPaths.append("\"").append(functionIdentifier).append(".zip\",");
            composeSourceCode(terraformFile.getParent(), functionIdentifier, metricValues.get(7).getValueString());
            if (runtime.startsWith("python")) {
                functionHandlers.append("\"main.handler\",");
            }
            functionTimeouts.append(metricValues.get(3).getValueNumber()).append(",");
            functionMemorySizes.append(metricValues.get(4).getValueNumber()).append(",");
            functionLayers.append("[],");
            functionRuntimes.append("\"").append(metricValues.get(6).getValueString().toLowerCase()).append("\",");
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

    private static String setFunctions() {
        return "resource \"aws_lambda_function\" \"lambda\" {\n" +
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
    }

    private static String setFunctionUrl() {
        return "resource \"aws_lambda_function_url\" \"function_url\" {\n" +
            "  count = length(local.function_names)\n" +
            "\n" +
            "  function_name      = aws_lambda_function.lambda[count.index].function_name\n" +
            "  authorization_type = \"NONE\"\n" +
            "}\n";
    }

    private static String setOutput() {
        return "output \"function-urls\" {\n" +
            "  value = aws_lambda_function_url.function_url\n" +
            "}\n";
    }

    private static String setLocalModule(String cloud, String region, String accessKey, String secretAccessKey,
                                         String sessionToken) {
        String moduleName = cloud + "_" + region;
        return String.format(
            "module \"%s\" {\n" +
            "  source = \"/%s\"\n" +
            "  access_key = var.aws_access_key\n" +
            "  secret_access_key = var.aws_secret_access_key\n" +
            "  session_token = var.aws_session_token\n" +
            "}", moduleName, moduleName);
    }

    private static void setUpMainFile() {
        String mainContent = getProviderString();
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

    // Src: https://www.baeldung.com/java-compress-and-uncompress
    private static void composeSourceCode(Path root, String functionIdentifier, String code) throws IOException {
        Path sourceCode = Path.of(root.toString(), functionIdentifier, "cloud_function.py");
        Files.createDirectories(sourceCode.getParent());
        Files.writeString(sourceCode, code, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        FileOutputStream fileOutputStream = new FileOutputStream(root + "\\" + functionIdentifier + ".zip");
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

        File handlerFile = new File("src\\main\\resources\\faas\\python\\main.py");
        File mainFile = new File(String.valueOf(sourceCode));
        File[] filesToZip = {handlerFile, mainFile};
        for (File fileToZip : filesToZip) {
            zipFile(fileToZip, zipOutputStream);
        }
        zipOutputStream.close();
        fileOutputStream.close();
    }

    private static void zipFile(File fileToZip, ZipOutputStream zipOutputStream) throws IOException {
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOutputStream.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while((length = fis.read(bytes)) >= 0) {
            zipOutputStream.write(bytes, 0, length);
        }
        fis.close();
    }

    private static void cleanUp() {
        // TODO: terraform destroy, delete temporary files
    }
}
