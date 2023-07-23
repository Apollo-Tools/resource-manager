package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.module.FaasModule;
import at.uibk.dps.rm.entity.deployment.module.TerraformModule;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.service.deployment.terraform.*;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to instantiate objects that are different types of file services.
 *
 * @author matthi-g
 */
public class TestFileServiceProvider {
    public static RegionFaasFileService createRegionFaasFileService(FileSystem fileSystem, Resource r1, Resource r2,
            Resource r3, Runtime runtime, Region region) {
        DeploymentPath path = new DeploymentPath(1L, TestConfigProvider.getConfig());
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime, false);
        Function f2 = TestFunctionProvider.createFunction(2L, "foo2", "false", runtime, false);
        FunctionDeployment fr1 = TestFunctionProvider.createFunctionDeployment(1L, f1, r1);
        FunctionDeployment fr2 = TestFunctionProvider.createFunctionDeployment(2L, f1, r2);
        FunctionDeployment fr3 = TestFunctionProvider.createFunctionDeployment(3L, f2, r2);
        FunctionDeployment fr4 = TestFunctionProvider.createFunctionDeployment(4L, f1, r3);
        List<FunctionDeployment> functionDeployments = List.of(fr1, fr2, fr3, fr4);
        long deploymentId = 1L;
        FaasModule module = new FaasModule(ResourceProviderEnum.AWS, region);
        String dockerUserName = "dockerUser";
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region);
        return new RegionFaasFileService(fileSystem, path, region, functionDeployments, deploymentId, module,
            dockerUserName, vpc);
    }

    public static RegionFaasFileService createRegionFaasFileServiceAllFaas(FileSystem fileSystem, Runtime runtime) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Resource r1 = TestResourceProvider.createResourceLambda(1L, region, 250.0, 512.0);
        Resource r2 = TestResourceProvider.createResourceEC2(2L, region, 100.0, 1024.0, "t2.micro");
        Resource r3 = TestResourceProvider.createResourceOpenFaas(3L, region, 100.0, 512.0, "http://localhost:8080",
            "user", "pw");
        return createRegionFaasFileService(fileSystem, r1, r2, r3, runtime, region);
    }

    public static RegionFaasFileService createRegionFaasFileServiceAllFaas(FileSystem fileSystem) {
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python3.8");
        return createRegionFaasFileServiceAllFaas(fileSystem, runtime);
    }

    public static FunctionPrepareService createFunctionFileService(Vertx vertx, Resource r1, Resource r2, Function f1,
                                                                Function f2) {
        FunctionDeployment fr1 = TestFunctionProvider.createFunctionDeployment(1L, f1, r1);
        FunctionDeployment fr2 = TestFunctionProvider.createFunctionDeployment(2L, f2, r2);
        List<FunctionDeployment> functionDeployments = List.of(fr1, fr2);
        DockerCredentials credentials = new DockerCredentials();
        credentials.setUsername("user");
        credentials.setAccessToken("access-token");
        DeploymentPath path = new DeploymentPath(1L, TestConfigProvider.getConfig());
        return new FunctionPrepareService(vertx, functionDeployments, path, List.of(credentials));
    }

    public static FunctionPrepareService createFunctionFileServiceNoFunctions(Vertx vertx) {
        List<FunctionDeployment> functionDeployments = new ArrayList<>();
        DockerCredentials credentials = new DockerCredentials();
        credentials.setUsername("user");
        credentials.setAccessToken("access-token");
        DeploymentPath path = new DeploymentPath(1L, TestConfigProvider.getConfig());
        return new FunctionPrepareService(vertx, functionDeployments, path, List.of(credentials));
    }

    public static FunctionPrepareService createFunctionFileServiceLambdaEc2Python(Vertx vertx) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python3.8");
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime, false);
        Function f2 = TestFunctionProvider.createFunction(2L, "foo2", "false", runtime, false);
        Resource r1 = TestResourceProvider.createResourceLambda(1L, region, 250.0, 512.0);
        Resource r2 = TestResourceProvider.createResourceEC2(2L, region, 150.0, 1024.0,"t2.micro");
        return createFunctionFileService(vertx, r1, r2, f1, f2);
    }

    public static FunctionPrepareService createFunctionFileServiceEC2OpenFaasPython(Vertx vertx) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python3.8");
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime, false);
        Function f2 = TestFunctionProvider.createFunction(2L, "foo2", "false", runtime, false);
        Resource r1 = TestResourceProvider.createResourceOpenFaas(1L,  region, 400.0, 200.0,
            "http://localhost:8081", "user1", "pw1");
        Resource r2 = TestResourceProvider.createResourceEC2(2L, region, 100.0, 1024.0, "t2.micro");
        return createFunctionFileService(vertx, r1, r2, f1, f2);
    }

    public static FunctionPrepareService createFunctionFileServiceEC2OpenFaasInvalidRuntime(Vertx vertx) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "invalid");
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime, false);
        Function f2 = TestFunctionProvider.createFunction(2L, "foo2", "false", runtime, false);
        Resource r1 = TestResourceProvider.createResourceOpenFaas(1L,  region, 400.0, 200.0,
            "http://localhost:8081", "user1", "pw1");
        Resource r2 = TestResourceProvider.createResourceEC2(2L, region, 100.0, 1024.0, "t2.micro");
        return createFunctionFileService(vertx, r1, r2, f1, f2);
    }

    public static FunctionPrepareService createFunctionFileServiceFunctionTwicePython(Vertx vertx) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python3.8");
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime, false);
        Resource r1 = TestResourceProvider.createResourceOpenFaas(1L,  region, 400.0, 200.0,
            "http://localhost:8081", "user1", "pw1");
        Resource r2 = TestResourceProvider.createResourceEC2(2L, region, 100.0, 1024.0, "t2.micro");
        return createFunctionFileService(vertx, r1, r2, f1, f1);
    }

    public static MainFileService createMainFileService(FileSystem fileSystem, List<TerraformModule> terraformModules) {
        Path rootFolder = Path.of("temp","test");
        return new MainFileService(fileSystem, rootFolder, terraformModules);
    }

    public static ContainerDeployFileService createContainerDeployFileService(FileSystem fileSystem, Path rootFolder,
            Deployment deployment) {
        ServiceDeployment serviceDeployment = TestServiceProvider.createServiceDeployment(1L, deployment);
        return new ContainerDeployFileService(fileSystem, rootFolder, serviceDeployment,
            deployment.getDeploymentId());
    }

    public static ContainerDeployFileService createContainerDeployFileService(FileSystem fileSystem, Path rootFolder,
            Resource resource, Deployment deployment) {
        ServiceDeployment serviceDeployment =
            TestServiceProvider.createServiceDeployment(1L, resource, deployment);
        return new ContainerDeployFileService(fileSystem, rootFolder, serviceDeployment,
            deployment.getDeploymentId());
    }

    public static ContainerPullFileService createContainerPullFileService(FileSystem fileSystem, Path rootFolder,
        Deployment deployment) {
        ServiceDeployment serviceDeployment = TestServiceProvider.createServiceDeployment(1L, deployment);
        return new ContainerPullFileService(fileSystem, rootFolder, List.of(serviceDeployment),
            deployment.getDeploymentId());
    }

    public static ContainerPullFileService createContainerPullFileService(FileSystem fileSystem, Path rootFolder,
        Deployment deployment, List<ServiceDeployment> serviceDeployments) {
        return new ContainerPullFileService(fileSystem, rootFolder, serviceDeployments,
            deployment.getDeploymentId());
    }
}
