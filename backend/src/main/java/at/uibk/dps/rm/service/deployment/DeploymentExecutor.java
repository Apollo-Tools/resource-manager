package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.service.deployment.terraform.AWSFileService;
import at.uibk.dps.rm.service.deployment.terraform.MainFileService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DeploymentExecutor {

    public Single<Long> deploy(JsonObject jsonObject) {
        DeployResourcesRequest deployResourcesRequest = jsonObject.mapTo(DeployResourcesRequest.class);
        // Do this for each region -> group resources by region
        List<Resource> resources = deployResourcesRequest.getResources();
        TerraformExecutor terraformExecutor = new TerraformExecutor(deployResourcesRequest.getCredentialsList());
        // TODO: get rid of these
        String region = "us-east-1";
        String awsRole = "LabRole";

        TerraformModule module = new TerraformModule(CloudProvider.AWS, "aws_" +
            region.replace("-", "_"));
        List<TerraformModule> modules = new ArrayList<>();
        modules.add(module);
        try {
            Path rootFolder = Paths.get("temp\\reservation_" + deployResourcesRequest.getReservationId());
            Path awsFolder = Paths.get(rootFolder + "\\" + module.getModuleName());
            AWSFileService awsUsEast1 = new AWSFileService(awsFolder, region,
                awsRole, resources, deployResourcesRequest.getReservationId());
            MainFileService mainFileService = new MainFileService(rootFolder, modules);
            // Create files
            mainFileService.setUpDirectory();
            awsUsEast1.setUpDirectory();
            terraformExecutor.setPluginCacheFolder(Paths.get("temp\\plugin_cache").toAbsolutePath());
            // Run terraform
            // TODO: make non blocking
            System.out.println("Return value: " + terraformExecutor.init(rootFolder));
            System.out.println("Return value: " + terraformExecutor.apply(rootFolder));
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return Single.just(1L);
    }
}
