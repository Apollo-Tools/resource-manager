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
        String accessKey = "ASIAQE3ZRVFQ2WXWQ4XH";
        String secretKey = "jpy8WifQrIpi/ZApXrmuwG6LarvkT9rUKFz/OFqF";
        String sessionToken = "FwoGZXIvYXdzEFQaDFpXf/8W63G7XqpyiyLUAQZ4PpN7/cp30Yav3+0Ai9slvC7wD4d4KgQfxtB08LkZUtGUjG+mrb/1X12VuUiuK0vcVOsZkVdtshKXdFlLsYeT6iJlpWk31PcypoI2cz91JetEQAXQ4pfm/KDprky7vFiBQsjiHmaKIS+TOf37lLD8hu8Jlozrt9r0Q8NpyoQTjWvmbnBjB/oUW7MDH/oRAbFpcjIj/qv2mPvz6uuLNozJ78aoPH9QvGSgpbAYTCjee6lfyiHdbV5U7fSVuR/Ki7OPU7JAKoRgTYM3fuFCpB5afxMSKJ++l5wGMi0nc1cHo7nN5bTpLVmDAH9aQ9gORrsSA7nGj9ZDpfNgrtvjhIYkaLp29zZjuy8=";
        String region = "us-east-1";
        String awsRole = "LabRole";
        String moduleName = "aws-" + region;
        long reservationId = 16;
        ResourceType resourceType = new ResourceType();
        resourceType.setTypeId(1L);
        resourceType.setResourceType("faas");
        Resource resource = new Resource();
        resource.setResourceId(1L);
        resource.setIsSelfManaged(false);
        resource.setResourceType(resourceType);
        resource.setMetricValues(setUpMetricValues(resource));

        List<Resource> resources = List.of(resource);
        try {
            Path rootFolder = Paths.get("temp\\reservation_" + reservationId);
            Path awsFolder = Paths.get(rootFolder + "\\" + moduleName);
            // Create files
            setUpMainDirectory(rootFolder, moduleName);
            setUpAwsDirectory(awsFolder, region, awsRole, resources, reservationId);
            // Run terraform
            ProcessBuilder builder = new ProcessBuilder("terraform",  "-chdir=" + rootFolder, "init");
            System.out.println("Return value: " + executeCli(builder));
            builder = new ProcessBuilder("terraform", "-chdir="  + rootFolder,
                "apply", "-auto-approve", "-var=\"aws_access_key=" + accessKey + "\"",
                "-var=\"aws_secret_access_key=" + secretKey + "\"", "-var=\"aws_session_token=" + sessionToken + "\"");
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

    private static List<MetricValue> setUpMetricValues(Resource resource) {
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
        return List.of(mv1, mv2, mv3, mv4, mv5, mv6, mv7, mv8);
    }

    private static String setProvider() {
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

    private static String setCredentialVariables(String cloud) {
        String preFix = cloud.equals("") ? "" : cloud + "_";
        return String.format(
            "variable \"%saccess_key\" {\n" +
            "  type = string\n" +
            "  default = \"\"\n" +
            "}\n" +
            "variable \"%ssecret_access_key\" {\n" +
            "  type = string\n" +
            "  default = \"\"\n" +
            "}\n" +
            "variable \"%ssession_token\" {\n" +
            "  type = string\n" +
            "  default = \"\"\n" +
            "}\n", preFix, preFix, preFix);
    }

    private static String setAWSProvider(String region) {
        return String.format(
            "provider \"aws\" {\n" +
            "  access_key = var.access_key\n" +
            "  secret_key = var.secret_access_key\n" +
            "  token = var.session_token\n" +
            "  region = \"%s\"\n" +
            "}\n", region);
    }

    private static String setAWSRole(String awsRole) {
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
            "  filename      = \"${path.module}/${local.function_paths[count.index]}\"\n" +
            "  function_name = local.function_names[count.index]\n" +
            "  role          = data.aws_iam_role.labRole.arn\n" +
            "  handler       = local.function_handlers[count.index]\n" +
            "  timeout       = local.function_timeouts[count.index]\n" +
            "  memory_size   = local.function_memory_sizes[count.index]\n" +
            "  layers        = local.function_layers[count.index]\n" +
            "  runtime       = local.function_runtimes[count.index]\n" +
            "  source_code_hash = filebase64sha256(\"${path.module}/${local.function_paths[count.index]}\")\n" +
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

    private static String setOutput(String module) {
        return String.format("output \"function_url\" {\n" +
            "  value = %s.function_url\n" +
            "}\n", module);
    }

    private static String setLocalModule(String moduleName) {
        return String.format(
            "module \"%s\" {\n" +
            "  source = \"./%s\"\n" +
            "  access_key = var.aws_access_key\n" +
            "  secret_access_key = var.aws_secret_access_key\n" +
            "  session_token = var.aws_session_token\n" +
            "}\n", moduleName, moduleName);
    }

    private static void setUpMainDirectory(Path rootFolder, String moduleName) throws IOException {
        Files.createDirectories(rootFolder);
        // Create files
        Path mainFile = Paths.get(rootFolder + "\\main.tf");
        String mainContent = setProvider() + setLocalModule(moduleName);
        Files.writeString(mainFile, mainContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE);

        Path variableFile = Paths.get(rootFolder + "\\variables.tf");
        String variableContent = setCredentialVariables("aws");
        Files.writeString(variableFile, variableContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE);

        Path outputFile = Paths.get(rootFolder + "\\outputs.tf");
        String outputContent = setOutput("module." + moduleName);
        Files.writeString(outputFile, outputContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

    private static void setUpAwsDirectory(Path rootFolder, String region, String awsRole, List<Resource> resources,
                                          long reservationId) throws IOException {
        Files.createDirectories(rootFolder);

        Path mainFile = Paths.get(rootFolder + "\\main.tf");
        String mainContent = setAWSProvider(region) + setAWSRole(awsRole) +
            setFunctionLocals(resources, reservationId, mainFile) + setFunctions() + setFunctionUrl();
        Files.writeString(mainFile, mainContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE);

        Path variableFile = Paths.get(rootFolder + "\\variables.tf");
        String variableContent = setCredentialVariables("");
        Files.writeString(variableFile, variableContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE);

        Path outputFile = Paths.get(rootFolder + "\\outputs.tf");
        String outputContent =  setOutput("aws_lambda_function_url");
        Files.writeString(outputFile, outputContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
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
