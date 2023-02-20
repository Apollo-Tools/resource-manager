package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.service.deployment.terraform.AWSFileService;
import at.uibk.dps.rm.service.deployment.terraform.MainFileService;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeploymentExecutor {

    public static void main(String[] chars) {
    JsonMapperConfig.configJsonMapper();
    JsonObject request = new JsonObject("{\"reservation_id\":25,\"credentials_list\":[{\"credentials_id\":3,\"access_key\":\"ASIAQE3ZRVFQRSST35OW\",\"secret_access_key\":\"9bwxSLExgfuI2Bo1TZxPSMiCBWrP7uulII0T/j53\",\"session_token\":\"FwoGZXIvYXdzEAIaDAuoyd37dibrI/NAIyLUAbqE1Xfd4ztgpkIC5ksS4C9w9eBuxqxBzhrs/B7aKF8XQB5kTggoawEYKHt4urqObFs3tloF1qOmvyxR5nRJTnOZUeQuI5Ob2dR6tj2uCC9tlw2zmbHJ/3HK37kUc5HzJX2XDKTa1/hocZH2N4we9emFHBnZ5Zr2hhlG60d6ljmCEomvBJEsxei/FqW2k9aZzDIC6DuHzIEsmauScr1kC/HBv2XKIh9Bnh01QsbS4FNHIjR/dMRpyDULVD46Zd4l9NylUs869A6fH+VGuHlNv/v78JTKKPzDx58GMi37oawrMSP+BH/KCUaH0dwW2ErgWBb4KVs3K7qNkuGtcjVYx64iEpvVJNpuBHU=\",\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"created_at\":1676576655118},\"created_at\":1676792867906}],\"function_resources\":[{\"function_resource_id\":1,\"resource\":{\"resource_id\":1,\"created_at\":1676576655944,\"updated_at\":1676580132936,\"resource_type\":{\"type_id\":1,\"resource_type\":\"faas\",\"created_at\":1676576655647},\"region\":{\"region_id\":3,\"name\":\"us-west-2\",\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"created_at\":1676576655118},\"created_at\":1676576655900},\"metric_values\":[{\"metric_value_id\":34,\"count\":null,\"value_number\":450.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":5,\"metric\":\"timeout\",\"description\":\"the maximum timeout for function executions\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655569,\"is_monitored\":false},\"created_at\":1676580500540,\"updated_at\":1676580500540},{\"metric_value_id\":37,\"count\":null,\"value_number\":512.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":6,\"metric\":\"memory-size\",\"description\":\"the memory size allocated for functions\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655614,\"is_monitored\":false},\"created_at\":1676580500686,\"updated_at\":1676580500686},{\"metric_value_id\":22,\"count\":10,\"value_number\":0.9980000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":1,\"metric\":\"availability\",\"description\":\"the availability of a resource\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655384,\"is_monitored\":true},\"created_at\":1676580499936,\"updated_at\":1676580499936},{\"metric_value_id\":25,\"count\":50,\"value_number\":43.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":2,\"metric\":\"latency\",\"description\":\"the latency between a resource and the resource manager\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655416,\"is_monitored\":true},\"created_at\":1676580500068,\"updated_at\":1676580500068},{\"metric_value_id\":28,\"count\":10,\"value_string\":null,\"value_bool\":true,\"metric\":{\"metric_id\":3,\"metric\":\"online\",\"description\":\"indicates if the resource is online\",\"metric_type\":{\"metric_type_id\":3,\"type\":\"boolean\"},\"created_at\":1676576655461,\"is_monitored\":true},\"created_at\":1676580500170,\"updated_at\":1676580500170},{\"metric_value_id\":31,\"count\":null,\"value_number\":8.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":4,\"metric\":\"cpu\",\"description\":\"the amount of cpu cores of a resource\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655497,\"is_monitored\":false},\"created_at\":1676580500316,\"updated_at\":1676580500316}],\"is_self_managed\":false},\"function\":{\"function_id\":1,\"name\":\"add2\",\"runtime\":{\"runtime_id\":1,\"name\":\"python3.8\",\"template_path\":\"./faas/python/cloud_function.py\",\"created_at\":1676576656097},\"code\":\"def main(json_input):\\n  input1 = json_input[\\\"input1\\\"]\\n  res = {\\n    \\\"output\\\": input1 + 1\\n  }\\n  return res\\n\",\"created_at\":1676576656155,\"updated_at\":1676576656155},\"created_at\":1676576656285,\"is_deployed\":false},{\"function_resource_id\":2,\"resource\":{\"resource_id\":2,\"created_at\":1676576655987,\"updated_at\":1676578322926,\"resource_type\":{\"type_id\":1,\"resource_type\":\"faas\",\"created_at\":1676576655647},\"region\":{\"region_id\":2,\"name\":\"us-east-1\",\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"created_at\":1676576655118},\"created_at\":1676576655856},\"metric_values\":[{\"metric_value_id\":32,\"count\":null,\"value_number\":4.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":4,\"metric\":\"cpu\",\"description\":\"the amount of cpu cores of a resource\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655497,\"is_monitored\":false},\"created_at\":1676580500469,\"updated_at\":1676580500469},{\"metric_value_id\":35,\"count\":null,\"value_number\":400.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":5,\"metric\":\"timeout\",\"description\":\"the maximum timeout for function executions\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655569,\"is_monitored\":false},\"created_at\":1676580500602,\"updated_at\":1676580500602},{\"metric_value_id\":38,\"count\":null,\"value_number\":128.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":6,\"metric\":\"memory-size\",\"description\":\"the memory size allocated for functions\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655614,\"is_monitored\":false},\"created_at\":1676580500736,\"updated_at\":1676580500736},{\"metric_value_id\":23,\"count\":10,\"value_number\":0.9990000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":1,\"metric\":\"availability\",\"description\":\"the availability of a resource\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655384,\"is_monitored\":true},\"created_at\":1676580499972,\"updated_at\":1676580499972},{\"metric_value_id\":26,\"count\":50,\"value_number\":80.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":2,\"metric\":\"latency\",\"description\":\"the latency between a resource and the resource manager\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655416,\"is_monitored\":true},\"created_at\":1676580500100,\"updated_at\":1676580500100},{\"metric_value_id\":29,\"count\":10,\"value_string\":null,\"value_bool\":false,\"metric\":{\"metric_id\":3,\"metric\":\"online\",\"description\":\"indicates if the resource is online\",\"metric_type\":{\"metric_type_id\":3,\"type\":\"boolean\"},\"created_at\":1676576655461,\"is_monitored\":true},\"created_at\":1676580500200,\"updated_at\":1676580500200}],\"is_self_managed\":true},\"function\":{\"function_id\":1,\"name\":\"add2\",\"runtime\":{\"runtime_id\":1,\"name\":\"python3.8\",\"template_path\":\"./faas/python/cloud_function.py\",\"created_at\":1676576656097},\"code\":\"def main(json_input):\\n  input1 = json_input[\\\"input1\\\"]\\n  res = {\\n    \\\"output\\\": input1 + 1\\n  }\\n  return res\\n\",\"created_at\":1676576656155,\"updated_at\":1676576656155},\"created_at\":1676576656330,\"is_deployed\":false}]}");
    request = new JsonObject("{\"reservation_id\":26,\"credentials_list\":[{\"credentials_id\":3,\"access_key\":\"ASIAQE3ZRVFQRSST35OW\",\"secret_access_key\":\"9bwxSLExgfuI2Bo1TZxPSMiCBWrP7uulII0T/j53\",\"session_token\":\"FwoGZXIvYXdzEAIaDAuoyd37dibrI/NAIyLUAbqE1Xfd4ztgpkIC5ksS4C9w9eBuxqxBzhrs/B7aKF8XQB5kTggoawEYKHt4urqObFs3tloF1qOmvyxR5nRJTnOZUeQuI5Ob2dR6tj2uCC9tlw2zmbHJ/3HK37kUc5HzJX2XDKTa1/hocZH2N4we9emFHBnZ5Zr2hhlG60d6ljmCEomvBJEsxei/FqW2k9aZzDIC6DuHzIEsmauScr1kC/HBv2XKIh9Bnh01QsbS4FNHIjR/dMRpyDULVD46Zd4l9NylUs869A6fH+VGuHlNv/v78JTKKPzDx58GMi37oawrMSP+BH/KCUaH0dwW2ErgWBb4KVs3K7qNkuGtcjVYx64iEpvVJNpuBHU=\",\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"created_at\":1676576655118},\"created_at\":1676792867906}],\"function_resources\":[{\"function_resource_id\":3,\"resource\":{\"resource_id\":3,\"created_at\":1676576656048,\"updated_at\":1676620309338,\"resource_type\":{\"type_id\":3,\"resource_type\":\"vm\",\"created_at\":1676576655723},\"region\":{\"region_id\":3,\"name\":\"us-west-2\",\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"created_at\":1676576655118},\"created_at\":1676576655900},\"metric_values\":[{\"metric_value_id\":33,\"count\":null,\"value_number\":16.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":4,\"metric\":\"cpu\",\"description\":\"the amount of cpu cores of a resource\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655497,\"is_monitored\":false},\"created_at\":1676580500504,\"updated_at\":1676580500504},{\"metric_value_id\":36,\"count\":null,\"value_number\":500.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":5,\"metric\":\"timeout\",\"description\":\"the maximum timeout for function executions\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655569,\"is_monitored\":false},\"created_at\":1676580500650,\"updated_at\":1676580500650},{\"metric_value_id\":39,\"count\":null,\"value_number\":1024.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":6,\"metric\":\"memory-size\",\"description\":\"the memory size allocated for functions\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655614,\"is_monitored\":false},\"created_at\":1676580500772,\"updated_at\":1676580500772},{\"metric_value_id\":24,\"count\":10,\"value_number\":0.9900000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":1,\"metric\":\"availability\",\"description\":\"the availability of a resource\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655384,\"is_monitored\":true},\"created_at\":1676580500030,\"updated_at\":1676580500030},{\"metric_value_id\":27,\"count\":50,\"value_number\":20.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":2,\"metric\":\"latency\",\"description\":\"the latency between a resource and the resource manager\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655416,\"is_monitored\":true},\"created_at\":1676580500138,\"updated_at\":1676580500138},{\"metric_value_id\":30,\"count\":10,\"value_string\":null,\"value_bool\":true,\"metric\":{\"metric_id\":3,\"metric\":\"online\",\"description\":\"indicates if the resource is online\",\"metric_type\":{\"metric_type_id\":3,\"type\":\"boolean\"},\"created_at\":1676576655461,\"is_monitored\":true},\"created_at\":1676580500245,\"updated_at\":1676580500245}],\"is_self_managed\":false},\"function\":{\"function_id\":1,\"name\":\"add2\",\"runtime\":{\"runtime_id\":1,\"name\":\"python3.8\",\"template_path\":\"./faas/python/cloud_function.py\",\"created_at\":1676576656097},\"code\":\"def main(json_input):\\n  input1 = json_input[\\\"input1\\\"]\\n  res = {\\n    \\\"output\\\": input1 + 1\\n  }\\n  return res\\n\",\"created_at\":1676576656155,\"updated_at\":1676576656155},\"created_at\":1676576656379,\"is_deployed\":false},{\"function_resource_id\":4,\"resource\":{\"resource_id\":3,\"created_at\":1676576656048,\"updated_at\":1676620309338,\"resource_type\":{\"type_id\":3,\"resource_type\":\"vm\",\"created_at\":1676576655723},\"region\":{\"region_id\":3,\"name\":\"us-west-2\",\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"created_at\":1676576655118},\"created_at\":1676576655900},\"metric_values\":[{\"metric_value_id\":33,\"count\":null,\"value_number\":16.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":4,\"metric\":\"cpu\",\"description\":\"the amount of cpu cores of a resource\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655497,\"is_monitored\":false},\"created_at\":1676580500504,\"updated_at\":1676580500504},{\"metric_value_id\":36,\"count\":null,\"value_number\":500.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":5,\"metric\":\"timeout\",\"description\":\"the maximum timeout for function executions\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655569,\"is_monitored\":false},\"created_at\":1676580500650,\"updated_at\":1676580500650},{\"metric_value_id\":39,\"count\":null,\"value_number\":1024.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":6,\"metric\":\"memory-size\",\"description\":\"the memory size allocated for functions\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655614,\"is_monitored\":false},\"created_at\":1676580500772,\"updated_at\":1676580500772},{\"metric_value_id\":24,\"count\":10,\"value_number\":0.9900000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":1,\"metric\":\"availability\",\"description\":\"the availability of a resource\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655384,\"is_monitored\":true},\"created_at\":1676580500030,\"updated_at\":1676580500030},{\"metric_value_id\":27,\"count\":50,\"value_number\":20.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":2,\"metric\":\"latency\",\"description\":\"the latency between a resource and the resource manager\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676576655416,\"is_monitored\":true},\"created_at\":1676580500138,\"updated_at\":1676580500138},{\"metric_value_id\":30,\"count\":10,\"value_string\":null,\"value_bool\":true,\"metric\":{\"metric_id\":3,\"metric\":\"online\",\"description\":\"indicates if the resource is online\",\"metric_type\":{\"metric_type_id\":3,\"type\":\"boolean\"},\"created_at\":1676576655461,\"is_monitored\":true},\"created_at\":1676580500245,\"updated_at\":1676580500245}],\"is_self_managed\":false},\"function\":{\"function_id\":2,\"name\":\"sub1\",\"runtime\":{\"runtime_id\":1,\"name\":\"python3.8\",\"template_path\":\"./faas/python/cloud_function.py\",\"created_at\":1676576656097},\"code\":\"def main(json_input):\\r\\n  input1 = json_input[\\\"input1\\\"]\\r\\n  res = {\\r\\n    \\\"output\\\": input1 - 1\\r\\n  }\\r\\n  return res\\r\\n\",\"created_at\":1676576656242,\"updated_at\":1676576656242},\"created_at\":1676576656423,\"is_deployed\":false}]}");
    DeploymentExecutor deploymentExecutor = new DeploymentExecutor();
    deploymentExecutor.deploy(request).subscribe();
    }

    public Single<Long> deploy(JsonObject jsonObject) {
        DeployResourcesRequest deployResourcesRequest = jsonObject.mapTo(DeployResourcesRequest.class);
        Map<Region, List<FunctionResource>> functionResources = deployResourcesRequest.getFunctionResources()
            .stream()
            .collect(Collectors.groupingBy(functionResource -> functionResource.getResource().getRegion()));
        TerraformExecutor terraformExecutor = new TerraformExecutor(deployResourcesRequest.getCredentialsList());
        try {
            List<TerraformModule> modules = new ArrayList<>();
            terraformExecutor.setPluginCacheFolder(Paths.get("temp\\plugin_cache").toAbsolutePath());
            Path rootFolder = Paths.get("temp\\reservation_" + deployResourcesRequest.getReservationId());
            for (Region region: functionResources.keySet()) {
                List<FunctionResource> regionFunctionResources = functionResources.get(region);
                TerraformModule module = regionDeployment(deployResourcesRequest, rootFolder, region.getName(),
                    regionFunctionResources);
                modules.add(module);
            }
            MainFileService mainFileService = new MainFileService(rootFolder, modules);
            // Create files
            mainFileService.setUpDirectory();
            // Run terraform
            // TODO: make non blocking
            //System.out.println("Return value: " + terraformExecutor.init(rootFolder));
            //System.out.println("Return value: " + terraformExecutor.apply(rootFolder));
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return Single.just(1L);
    }

    protected TerraformModule regionDeployment(DeployResourcesRequest deployResourcesRequest, Path rootFolder,String region,
                                    List<FunctionResource> functionResources) throws IOException {
        // TODO: get rid of these
        String awsRole = "LabRole";

        TerraformModule module = new TerraformModule(CloudProvider.AWS, "aws_" +
            region.replace("-", "_"));
        Path awsFolder = Paths.get(rootFolder + "\\" + module.getModuleName());
        AWSFileService fileService = new AWSFileService(awsFolder, region,
            awsRole, functionResources, deployResourcesRequest.getReservationId(), module);
        fileService.setUpDirectory();
        return module;
    }
}
