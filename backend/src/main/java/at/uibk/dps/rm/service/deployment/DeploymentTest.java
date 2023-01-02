package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.service.deployment.terraform.AWSFileService;
import at.uibk.dps.rm.service.deployment.terraform.MainFileService;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DeploymentTest {

    public static void main(String[] args) {
        String accessKey = "ASIAQE3ZRVFQUWDSWQFG";
        String secretKey = "LpjP3ErHmiMVgigXLttxOgGoeVxzlmnoPQYYgyBo";
        String sessionToken = "FwoGZXIvYXdzEGwaDBz+oh65X7ns9hZuuyLUAdMHu8h4RN2CMbFTOSPrJGUrminNm+Y5WNBuztvVcbTe07wrEyGHRLm9aKYcMUAENiIQiQVFBHHlcYJkEOzOs0+Yto/I9rd72hhvNBG4e17ro+67tn9DXVBGDWdjx9QX4qUcBs9eCYCiYysM3C6Wx/pmtG3pwPKrYFScrfCEFbwS7zV5+sPtdLQD8qLCzCjQfIsozQxw1XkgwfXq/RhAV3BLcPaQqlZmsSaG8IPLYe8ccp0k6PHmQp5qdPYEcNuOQl1ykVBXBAa879VBTEDL0ag5k4jbKP3dnJwGMi23ka74uYOGIn3rKV3Ajk4DbDfUtVNUJlISg7rfBXbYe5O8QbbZMePoBcopz98=";
        ResourceProvider resourceProvider = new ResourceProvider();
        resourceProvider.setProviderId(1L);
        resourceProvider.setProvider("aws");
        Credentials credentials = new Credentials();
        credentials.setCredentialsId(1L);
        credentials.setAccessKey(accessKey);
        credentials.setSecretAccessKey(secretKey);
        credentials.setSessionToken(sessionToken);
        credentials.setResourceProvider(resourceProvider);
        TerraformExecutor terraformExecutor = new TerraformExecutor(List.of(credentials));
        String region = "us-east-1";
        String awsRole = "LabRole";
        TerraformModule module = new TerraformModule(CloudProvider.AWS, "aws_" + region.replace("-", "_"));
        long reservationId = 24;
        ResourceType resourceType = new ResourceType();
        resourceType.setTypeId(1L);
        resourceType.setResourceType("faas");
        Resource resource = new Resource();
        resource.setResourceId(1L);
        resource.setIsSelfManaged(false);
        resource.setResourceType(resourceType);
        resource.setMetricValues(setUpMetricValues(resource));
        List<Resource> resources = List.of(resource);

        List<TerraformModule> modules = new ArrayList<>();
        modules.add(module);
        try {
            Path rootFolder = Paths.get("temp\\reservation_" + reservationId);
            Path awsFolder = Paths.get(rootFolder + "\\" + module.getModuleName());
            AWSFileService awsUsEast1 = new AWSFileService(awsFolder, region, awsRole, resources, reservationId);
            MainFileService mainFileService = new MainFileService(rootFolder, modules);
            // Create files
            mainFileService.setUpDirectory();
            awsUsEast1.setUpDirectory();
            terraformExecutor.setPluginCacheFolder(Paths.get("temp\\plugin_cache").toAbsolutePath());
            // Run terraform
            System.out.println("Return value: " + terraformExecutor.init(rootFolder));
            System.out.println("Return value: " + terraformExecutor.apply(rootFolder));
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
}
