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
    JsonObject request = new JsonObject("{\"reservation_id\":8,\"credentials_list\":[{\"credentials_id\":1,\"access_key\":\"ASIAQE3ZRVFQXVONR7PL\",\"secret_access_key\":\"q8+/j+YYkb3EP5Vv6EbqGQcts+R7eIMjXpsIMmyb\",\"session_token\":\"FwoGZXIvYXdzEMX//////////wEaDPEG4SLDm2KglrH0MyLUAUka1JQ0hojYXVZ7NT+r6O+1Y5fBorieCyXtd/fY4ns9zwdGTNGsVnAmlLPqX5FQPcxIx4lHP5AudgEaMhdcVLlvmuEm3etRqUzPc+ClUSThJPzZPTvTWUf7/+xQyryQ7ufM6ZJ/zCxF62sGf/jZZ3Q/bLxaAx6xPN4vQ6Sf86cUFF40BgeeEynOcmARVVh1O5R0j217dCV25TkOHfwzUWxMSWKJ8HZEAx9YUmmf1jNFRY8yvAsWGi7G8J3L+jhRQTWLfsQHokUWs5LYN9lYcgpTf6ftKPSHup8GMi3NDSDveFTNnj8VdMT1FjzuPhmwHX3WYlALpAkKbJy9/Tv8rSk7F8mhACmtLLw=\",\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"created_at\":1676454307155},\"created_at\":1676531556943}],\"function_resources\":[{\"function_resource_id\":1,\"resource\":{\"resource_id\":1,\"created_at\":1676454308429,\"updated_at\":1676454308429,\"resource_type\":{\"type_id\":1,\"resource_type\":\"faas\",\"created_at\":1676454308008},\"region\":{\"region_id\":1,\"name\":\"us-east-1\",\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"created_at\":1676454307155},\"created_at\":1676454308280},\"metric_values\":[{\"metric_value_id\":1,\"count\":10,\"value_number\":0.9980000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":1,\"metric\":\"availability\",\"description\":\"the availability of a resource\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676454307564,\"is_monitored\":true},\"created_at\":1676454309056,\"updated_at\":1676454309056},{\"metric_value_id\":4,\"count\":50,\"value_number\":43.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":2,\"metric\":\"latency\",\"description\":\"the latency between a resource and the resource manager\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676454307652,\"is_monitored\":true},\"created_at\":1676454309243,\"updated_at\":1676454309243},{\"metric_value_id\":7,\"count\":10,\"value_string\":null,\"value_bool\":true,\"metric\":{\"metric_id\":3,\"metric\":\"online\",\"description\":\"indicates if the resource is online\",\"metric_type\":{\"metric_type_id\":3,\"type\":\"boolean\"},\"created_at\":1676454307807,\"is_monitored\":true},\"created_at\":1676454309383,\"updated_at\":1676454309383},{\"metric_value_id\":10,\"count\":null,\"value_number\":8.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":4,\"metric\":\"cpu\",\"description\":\"the amount of cpu cores of a resource\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676454307894,\"is_monitored\":false},\"created_at\":1676454309563,\"updated_at\":1676454309563}],\"is_self_managed\":false},\"function\":{\"function_id\":1,\"name\":\"add1\",\"runtime\":{\"runtime_id\":1,\"name\":\"python3.8\",\"template_path\":\"./faas/python/cloud_function.py\",\"created_at\":1676454308661},\"code\":\"def main(json_input):\\r\\n  input1 = json_input[\\\"input1\\\"]\\r\\n  res = {\\r\\n    \\\"output\\\": input1 + 1\\r\\n  }\\r\\n  return res\\r\\n\",\"created_at\":1676454308752,\"updated_at\":1676454308752},\"created_at\":1676454308860,\"is_deployed\":false},{\"function_resource_id\":4,\"resource\":{\"resource_id\":3,\"created_at\":1676454308558,\"updated_at\":1676454308558,\"resource_type\":{\"type_id\":3,\"resource_type\":\"vm\",\"created_at\":1676454308162},\"region\":{\"region_id\":2,\"name\":\"us-west-2\",\"resource_provider\":{\"provider_id\":1,\"provider\":\"aws\",\"created_at\":1676454307155},\"created_at\":1676454308280},\"metric_values\":[{\"metric_value_id\":3,\"count\":10,\"value_number\":0.9900000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":1,\"metric\":\"availability\",\"description\":\"the availability of a resource\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676454307564,\"is_monitored\":true},\"created_at\":1676454309191,\"updated_at\":1676454309191},{\"metric_value_id\":6,\"count\":50,\"value_number\":20.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":2,\"metric\":\"latency\",\"description\":\"the latency between a resource and the resource manager\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676454307652,\"is_monitored\":true},\"created_at\":1676454309335,\"updated_at\":1676454309335},{\"metric_value_id\":9,\"count\":10,\"value_string\":null,\"value_bool\":true,\"metric\":{\"metric_id\":3,\"metric\":\"online\",\"description\":\"indicates if the resource is online\",\"metric_type\":{\"metric_type_id\":3,\"type\":\"boolean\"},\"created_at\":1676454307807,\"is_monitored\":true},\"created_at\":1676454309479,\"updated_at\":1676454309479},{\"metric_value_id\":12,\"count\":null,\"value_number\":16.0000000000,\"value_string\":null,\"value_bool\":null,\"metric\":{\"metric_id\":4,\"metric\":\"cpu\",\"description\":\"the amount of cpu cores of a resource\",\"metric_type\":{\"metric_type_id\":1,\"type\":\"number\"},\"created_at\":1676454307894,\"is_monitored\":false},\"created_at\":1676454309660,\"updated_at\":1676454309660}],\"is_self_managed\":false},\"function\":{\"function_id\":2,\"name\":\"sub1\",\"runtime\":{\"runtime_id\":1,\"name\":\"python3.8\",\"template_path\":\"./faas/python/cloud_function.py\",\"created_at\":1676454308661},\"code\":\"def main(json_input):\\r\\n  input1 = json_input[\\\"input1\\\"]\\r\\n  res = {\\r\\n    \\\"output\\\": input1 - 1\\r\\n  }\\r\\n  return res\\r\\n\",\"created_at\":1676454308812,\"updated_at\":1676454308812},\"created_at\":1676454309015,\"is_deployed\":false}]}");
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
