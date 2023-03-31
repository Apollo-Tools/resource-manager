package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.deployment.TerraformModule;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.service.deployment.terraform.AWSFileService;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TestAWSFileServiceProvider {
    public static AWSFileService createAWSFileService(FileSystem fileSystem, Resource r1, Resource r2, Resource r3,
                                                      Runtime runtime, Region region) {
        Path rootFolder = Paths.get("temp\\test");
        Path functionsDir = Path.of(String.valueOf(rootFolder), "functions");
        String awsRole = "LabRole";
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime);
        Function f2 = TestFunctionProvider.createFunction(2L, "foo2", "false", runtime);
        FunctionResource fr1 = TestFunctionProvider.createFunctionResource(1L, f1, r1);
        FunctionResource fr2 = TestFunctionProvider.createFunctionResource(2L, f1, r2);
        FunctionResource fr3 = TestFunctionProvider.createFunctionResource(3L, f2, r2);
        FunctionResource fr4 = TestFunctionProvider.createFunctionResource(3L, f1, r3);
        List<FunctionResource> functionResources = List.of(fr1, fr2, fr3, fr4);
        long reservationId = 1L;
        TerraformModule module = new TerraformModule(CloudProvider.AWS, region.getResourceProvider()
            .getProvider() + "_" + region.getName().replace("-", "_"));
        String dockerUserName = "dockerUser";
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region);
        return new AWSFileService(fileSystem, rootFolder, functionsDir, region, awsRole,
            functionResources, reservationId, module, dockerUserName, vpc);
    }

    public static AWSFileService createAWSFileServiceFaasVMEdge(FileSystem fileSystem, Runtime runtime) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Resource r1 = TestResourceProvider.createResourceFaaS(1L, region, 250.0, 512.0);
        Resource r2 = TestResourceProvider.createResourceVM(2L, region, "t2.micro");
        Resource r3 = TestResourceProvider.createResourceEdge(3L, 300.0, 2048.0);
        return createAWSFileService(fileSystem, r1, r2, r3, runtime, region);
    }

    public static AWSFileService createAWSFileServiceFaasVMEdge(FileSystem fileSystem) {
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        return createAWSFileServiceFaasVMEdge(fileSystem, runtime);
    }

    public static AWSFileService createAWSFileServiceFaasEdge(FileSystem fileSystem) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        Resource r1 = TestResourceProvider.createResourceEdge(1L,250.0, 512.0);
        Resource r2 = TestResourceProvider.createResourceFaaS(1L, region, 250.0, 512.0);
        Resource r3 = TestResourceProvider.createResourceEdge(1L,125.0, 2048.0);
        return createAWSFileService(fileSystem, r1, r2, r3, runtime, region);
    }

    public static AWSFileService createAWSFileServiceVMEdge(FileSystem fileSystem) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        Resource r1 = TestResourceProvider.createResourceEdge(1L,250.0, 512.0);
        Resource r2 = TestResourceProvider.createResourceVM(2L, region, "t2.micro");
        Resource r3 = TestResourceProvider.createResourceVM(3L, region, "t3.micro");
        return createAWSFileService(fileSystem, r1, r2, r3, runtime, region);
    }
}
