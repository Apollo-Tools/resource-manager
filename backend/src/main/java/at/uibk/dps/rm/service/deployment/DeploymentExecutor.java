package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.service.deployment.terraform.AWSFileService;
import at.uibk.dps.rm.service.deployment.terraform.EdgeFileService;
import at.uibk.dps.rm.service.deployment.terraform.MainFileService;
import at.uibk.dps.rm.service.deployment.terraform.FunctionFileService;
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
    request = new JsonObject("{\"docker_username\":\"user\", \"docker_password\": \"pw\", \"reservation_id\":8,\"credentials_list\":[{\"credentials_id\":1,\"access_key\":\"ASIAQE3ZRVFQ3765JGEZ\",\"secret_access_key\":\"Tvcpg3aZycqEyWyqIu1n97yv6+AXI1qyVNuZdkDU\",\"session_token\":\"FwoGZXIvYXdzECMaDO5CvfjGJhE/1giiXyLUAeKZLnISy/wt/haw+HryoH6CqhBBhuCM5VVp3nwIiEXeJTejwWq2R2xnteYrA9fwp3q+v8XvcnSJQVlsEN5sRP22TnMOmHvD/D/6RLmDfxUuAIfOOp6afmtUMWSssxC9Niuvaq0OiX/Sq/8RBj7ct+Lxu1TWMqnPj6A65J6p8q9SbHiR7mx8MngTsp9FP9ffAG3tv3kdHqGwyLhiYKY45kW5oIm/yTJCHGfg0N0QzILlNG6auxC25fk4PHF/IYvA/LKEMtsKutgtejKK1IMz2PukOKQrKNzhzp8GMi22hH3ctupPbS+qDVAnrWKHdcyJyR9hCAdAaUiufuLLRAyP+WagpcU1v2tObvE=\",\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"created_at\":1675859424263},\"created_at\":1676914556724}],\"function_resources\":[{\"function_resource_id\":1,\"resource\":{\"resource_id\":1,\"created_at\":1675859424581,\"updated_at\":1676914658522,\"resource_type\":{\"type_id\":1,\"resource_type\":\"faas\",\"created_at\":1675859424466},\"region\":{\"region_id\":2,\"name\":\"us-east-1\",\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"created_at\":1675859424263},\"created_at\":1675859424549},\"metric_values\":[],\"is_self_managed\":false},\"function\":{\"function_id\":1,\"name\":\"add1\",\"runtime\":{\"runtime_id\":1,\"name\":\"python3.8\",\"template_path\":\"./faas/python/cloud_function.py\",\"created_at\":1675859424617},\"code\":\"def main(json_input):\\r\\n  input1 = json_input[\\\"input1\\\"]\\r\\n  res = {\\r\\n    \\\"output\\\": input1 + 1\\r\\n  }\\r\\n  return res\\r\\n\",\"created_at\":1675859424635,\"updated_at\":1675859424635},\"created_at\":1675859424667,\"is_deployed\":false},{\"function_resource_id\":17,\"resource\":{\"resource_id\":7,\"created_at\":1675875732665,\"updated_at\":1675875732665,\"resource_type\":{\"type_id\":1,\"resource_type\":\"faas\",\"created_at\":1675859424466},\"region\":{\"region_id\":1,\"name\":\"us-west-2\",\"resource_provider\":{\"provider_id\":2,\"provider\":\"azure\",\"created_at\":1675859424280},\"created_at\":1675859424532},\"metric_values\":[],\"is_self_managed\":true},\"function\":{\"function_id\":1,\"name\":\"add1\",\"runtime\":{\"runtime_id\":1,\"name\":\"python3.8\",\"template_path\":\"./faas/python/cloud_function.py\",\"created_at\":1675859424617},\"code\":\"def main(json_input):\\r\\n  input1 = json_input[\\\"input1\\\"]\\r\\n  res = {\\r\\n    \\\"output\\\": input1 + 1\\r\\n  }\\r\\n  return res\\r\\n\",\"created_at\":1675859424635,\"updated_at\":1675859424635},\"created_at\":1675889196627,\"is_deployed\":false},{\"function_resource_id\":18,\"resource\":{\"resource_id\":8,\"created_at\":1675877728032,\"updated_at\":1676914658522,\"resource_type\":{\"type_id\":1,\"resource_type\":\"faas\",\"created_at\":1675859424466},\"region\":{\"region_id\":2,\"name\":\"us-east-1\",\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"created_at\":1675859424263},\"created_at\":1675859424549},\"metric_values\":[],\"is_self_managed\":true},\"function\":{\"function_id\":1,\"name\":\"add1\",\"runtime\":{\"runtime_id\":1,\"name\":\"python3.8\",\"template_path\":\"./faas/python/cloud_function.py\",\"created_at\":1675859424617},\"code\":\"def main(json_input):\\r\\n  input1 = json_input[\\\"input1\\\"]\\r\\n  res = {\\r\\n    \\\"output\\\": input1 + 1\\r\\n  }\\r\\n  return res\\r\\n\",\"created_at\":1675859424635,\"updated_at\":1675859424635},\"created_at\":1675889196627,\"is_deployed\":false},{\"function_resource_id\":20,\"resource\":{\"resource_id\":3,\"created_at\":1675859424606,\"updated_at\":1675859424606,\"resource_type\":{\"type_id\":3,\"resource_type\":\"vm\",\"created_at\":1675859424499},\"region\":{\"region_id\":1,\"name\":\"us-west-2\",\"resource_provider\":{\"provider_id\":2,\"provider\":\"azure\",\"created_at\":1675859424280},\"created_at\":1675859424532},\"metric_values\":[{\"metric_value_id\":3,\"count\":10,\"value_number\":0.9900000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":1,\"metric\":\"availability\",\"description\":\"the availability of a resource\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1675859424370,\"is_monitored\":true},\"created_at\":1675859424760,\"updated_at\":1675859424760},{\"metric_value_id\":6,\"count\":50,\"value_number\":20.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":2,\"metric\":\"latency\",\"description\":\"the latency between a resource and the resource manager\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1675859424386,\"is_monitored\":true},\"created_at\":1675859424799,\"updated_at\":1675859424799},{\"metric_value_id\":9,\"count\":10,\"value_string\":null,\"value_bool\":true,\"metric\":{\"metric_id\":3,\"metric\":\"online\",\"description\":\"indicates if the resource is online\",\"metric_type\":{\"metric_type_id\":3,\"type\":\"boolean\"},\"created_at\":1675859424399,\"is_monitored\":true},\"created_at\":1675859424854,\"updated_at\":1675859424854},{\"metric_value_id\":12,\"count\":null,\"value_number\":16.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":4,\"metric\":\"cpu\",\"description\":\"the amount of cpu cores of a resource\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1675859424417,\"is_monitored\":false},\"created_at\":1675859424900,\"updated_at\":1675859424900}],\"is_self_managed\":false},\"function\":{\"function_id\":1,\"name\":\"add1\",\"runtime\":{\"runtime_id\":1,\"name\":\"python3.8\",\"template_path\":\"./faas/python/cloud_function.py\",\"created_at\":1675859424617},\"code\":\"def main(json_input):\\r\\n  input1 = json_input[\\\"input1\\\"]\\r\\n  res = {\\r\\n    \\\"output\\\": input1 + 1\\r\\n  }\\r\\n  return res\\r\\n\",\"created_at\":1675859424635,\"updated_at\":1675859424635},\"created_at\":1675889221209,\"is_deployed\":false},{\"function_resource_id\":21,\"resource\":{\"resource_id\":5,\"created_at\":1675863192979,\"updated_at\":1675863192979,\"resource_type\":{\"type_id\":1,\"resource_type\":\"faas\",\"created_at\":1675859424466},\"region\":{\"region_id\":1,\"name\":\"us-west-2\",\"resource_provider\":{\"provider_id\":2,\"provider\":\"azure\",\"created_at\":1675859424280},\"created_at\":1675859424532},\"metric_values\":[],\"is_self_managed\":true},\"function\":{\"function_id\":1,\"name\":\"add1\",\"runtime\":{\"runtime_id\":1,\"name\":\"python3.8\",\"template_path\":\"./faas/python/cloud_function.py\",\"created_at\":1675859424617},\"code\":\"def main(json_input):\\r\\n  input1 = json_input[\\\"input1\\\"]\\r\\n  res = {\\r\\n    \\\"output\\\": input1 + 1\\r\\n  }\\r\\n  return res\\r\\n\",\"created_at\":1675859424635,\"updated_at\":1675859424635},\"created_at\":1675889221209,\"is_deployed\":false},{\"function_resource_id\":22,\"resource\":{\"resource_id\":4,\"created_at\":1675863179041,\"updated_at\":1675863179041,\"resource_type\":{\"type_id\":3,\"resource_type\":\"vm\",\"created_at\":1675859424499},\"region\":{\"region_id\":2,\"name\":\"us-east-1\",\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"created_at\":1675859424263},\"created_at\":1675859424549},\"metric_values\":[],\"is_self_managed\":true},\"function\":{\"function_id\":2,\"name\":\"sub1\",\"runtime\":{\"runtime_id\":1,\"name\":\"python3.8\",\"template_path\":\"./faas/python/cloud_function.py\",\"created_at\":1675859424617},\"code\":\"def main(json_input):\\r\\n  input1 = json_input[\\\"input1\\\"]\\r\\n  res = {\\r\\n    \\\"output\\\": input1 - 1\\r\\n  }\\r\\n  return res\\r\\n\",\"created_at\":1675859424654,\"updated_at\":1675859424654},\"created_at\":1675889244013,\"is_deployed\":false},{\"function_resource_id\":26,\"resource\":{\"resource_id\":5,\"created_at\":1675863192979,\"updated_at\":1675863192979,\"resource_type\":{\"type_id\":1,\"resource_type\":\"faas\",\"created_at\":1675859424466},\"region\":{\"region_id\":1,\"name\":\"us-west-2\",\"resource_provider\":{\"provider_id\":2,\"provider\":\"azure\",\"created_at\":1675859424280},\"created_at\":1675859424532},\"metric_values\":[],\"is_self_managed\":true},\"function\":{\"function_id\":4,\"name\":\"Testfunc\",\"runtime\":{\"runtime_id\":1,\"name\":\"python3.8\",\"template_path\":\"./faas/python/cloud_function.py\",\"created_at\":1675859424617},\"code\":\"faefgewafw\",\"created_at\":1675889278251,\"updated_at\":1675889278251},\"created_at\":1675889283973,\"is_deployed\":false}]}");
    DeploymentExecutor deploymentExecutor = new DeploymentExecutor();
    deploymentExecutor.deploy(request).subscribe();
    }

    public Single<Long> deploy(JsonObject jsonObject) {
        DeployResourcesRequest deployResourcesRequest = jsonObject.mapTo(DeployResourcesRequest.class);
        Map<Region, List<FunctionResource>> functionResources = deployResourcesRequest.getFunctionResources()
            .stream()
            .collect(Collectors.groupingBy(functionResource -> functionResource.getResource().getRegion()));
        Map<Region, VPC> regionVPCMap = deployResourcesRequest.getVpcList()
            .stream()
            .collect(Collectors.toMap(VPC::getRegion, vpc -> vpc, (vpc1, vpc2) -> vpc1));
        TerraformExecutor terraformExecutor = new TerraformExecutor(deployResourcesRequest.getCredentialsList());
        try {
            List<TerraformModule> modules = new ArrayList<>();
            terraformExecutor.setPluginCacheFolder(Paths.get("temp\\plugin_cache").toAbsolutePath());
            Path rootFolder = Paths.get("temp\\reservation_" + deployResourcesRequest.getReservationId());
            Path functionsDir = Path.of(rootFolder.toString(), "functions");
            // TF: create all deployment files
            for (Region region: functionResources.keySet()) {
                List<FunctionResource> regionFunctionResources = functionResources.get(region);
                TerraformModule module;
                if (region.getName().equals("edge")) {
                    // TF: Edge resources
                    module = edgeDeployment(deployResourcesRequest, rootFolder);
                } else {
                    // TF: Cloud resources
                    module = cloudDeployment(deployResourcesRequest, rootFolder, functionsDir,
                        region, regionFunctionResources, regionVPCMap);
                }
                modules.add(module);
            }

            // TF: main files
            MainFileService mainFileService = new MainFileService(rootFolder, modules);
            mainFileService.setUpDirectory();
            // Build functions
            FunctionFileService functionFileService = new FunctionFileService(deployResourcesRequest.getFunctionResources(),
                functionsDir, deployResourcesRequest.getDockerCredentials());
            functionFileService.packageCode();
            //
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

    // TODO: Rework for other cloud providers
    protected TerraformModule cloudDeployment(DeployResourcesRequest deployResourcesRequest, Path rootFolder,
                                              Path functionsDir, Region region, List<FunctionResource> functionResources,
                                              Map<Region, VPC> regionVPCMap)
        throws IOException {
        // TODO: get rid of these
        String awsRole = "LabRole";
        String provider = region.getResourceProvider().getProvider();
        TerraformModule module = new TerraformModule(CloudProvider.AWS, provider + "_" +
            region.getName().replace("-", "_"));
        Path awsFolder = Paths.get(rootFolder + "\\" + module.getModuleName());
        AWSFileService fileService = new AWSFileService(awsFolder, functionsDir, region,
            awsRole, functionResources, deployResourcesRequest.getReservationId(), module,
            deployResourcesRequest.getDockerCredentials().getUsername(), regionVPCMap.get(region));
        fileService.setUpDirectory();
        return module;
    }

    protected TerraformModule edgeDeployment(DeployResourcesRequest deployResourcesRequest, Path rootFolder) throws IOException {
        TerraformModule module = new TerraformModule(CloudProvider.EDGE, "edge");
        Path edgeFolder = Paths.get(rootFolder.toString(), module.getModuleName());
        EdgeFileService edgeService = new EdgeFileService(edgeFolder, deployResourcesRequest.getFunctionResources(),
            deployResourcesRequest.getReservationId(), deployResourcesRequest.getDockerCredentials().getUsername());
        edgeService.setUpDirectory();
        return module;
    }

}
