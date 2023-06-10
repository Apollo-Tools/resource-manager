package at.uibk.dps.rm.testutil.objectprovider;

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
import java.nio.file.Paths;
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
        Path rootFolder = Paths.get("temp\\test");
        Path functionsDir = Path.of(String.valueOf(rootFolder), "functions");
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime);
        Function f2 = TestFunctionProvider.createFunction(2L, "foo2", "false", runtime);
        FunctionDeployment fr1 = TestFunctionProvider.createFunctionReservation(1L, f1, r1);
        FunctionDeployment fr2 = TestFunctionProvider.createFunctionReservation(2L, f1, r2);
        FunctionDeployment fr3 = TestFunctionProvider.createFunctionReservation(3L, f2, r2);
        FunctionDeployment fr4 = TestFunctionProvider.createFunctionReservation(4L, f1, r3);
        List<FunctionDeployment> functionDeployments = List.of(fr1, fr2, fr3, fr4);
        long reservationId = 1L;
        FaasModule module = new FaasModule(ResourceProviderEnum.AWS, region);
        String dockerUserName = "dockerUser";
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region);
        return new RegionFaasFileService(fileSystem, rootFolder, functionsDir, region, functionDeployments,
            reservationId, module, dockerUserName, vpc);
    }

    public static RegionFaasFileService createRegionFaasFileServiceFaasVMEdge(FileSystem fileSystem, Runtime runtime) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Resource r1 = TestResourceProvider.createResourceFaaS(1L, region, 250.0, 512.0);
        Resource r2 = TestResourceProvider.createResourceVM(2L, region, "t2.micro");
        Resource r3 = TestResourceProvider.createResourceEdge(3L, "http://localhost:8080", "user", "pw");
        return createRegionFaasFileService(fileSystem, r1, r2, r3, runtime, region);
    }

    public static RegionFaasFileService createRegionFaasFileServiceFaasVMEdge(FileSystem fileSystem) {
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        return createRegionFaasFileServiceFaasVMEdge(fileSystem, runtime);
    }

    public static RegionFaasFileService createRegionFaasFileServiceFaasEdge(FileSystem fileSystem) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        Resource r1 = TestResourceProvider.createResourceEdge(1L, "http://localhost:8080", "user", "pw");
        Resource r2 = TestResourceProvider.createResourceFaaS(1L, region, 250.0, 512.0);
        Resource r3 = TestResourceProvider.createResourceEdge(3L, "http://localhost:8082", "user", "pw");
        return createRegionFaasFileService(fileSystem, r1, r2, r3, runtime, region);
    }

    public static RegionFaasFileService createRegionFaasFileServiceVMEdge(FileSystem fileSystem) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        Resource r1 = TestResourceProvider.createResourceEdge(1L, "http://localhost:8080", "user", "pw");
        Resource r2 = TestResourceProvider.createResourceVM(2L, region, "t2.micro");
        Resource r3 = TestResourceProvider.createResourceVM(3L, region, "t3.micro");
        return createRegionFaasFileService(fileSystem, r1, r2, r3, runtime, region);
    }

    public static RegionFaasFileService createRegionFaasFileServiceEdge(FileSystem fileSystem) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        Resource r1 = TestResourceProvider.createResourceEdge(1L, "http://localhost:8081", "user1", "pw1");
        Resource r2 = TestResourceProvider.createResourceEdge(2L, "http://localhost:8082", "user2", "pw2");
        Resource r3 = TestResourceProvider.createResourceEdge(3L, "http://localhost:8083", "user3", "pw3");
        return createRegionFaasFileService(fileSystem, r1, r2, r3, runtime, region);
    }


    public static FunctionPrepareService createFunctionFileService(Vertx vertx, Resource r1, Resource r2, Function f1,
                                                                Function f2) {
        FunctionDeployment fr1 = TestFunctionProvider.createFunctionReservation(1L, f1, r1);
        FunctionDeployment fr2 = TestFunctionProvider.createFunctionReservation(2L, f2, r2);
        List<FunctionDeployment> functionReservations = List.of(fr1, fr2);
        DockerCredentials credentials = new DockerCredentials();
        credentials.setUsername("user");
        credentials.setAccessToken("access-token");
        Path functionsDir = Paths.get("temp\\test\\functions");
        return new FunctionPrepareService(vertx, functionReservations, functionsDir, credentials);
    }

    public static FunctionPrepareService createFunctionFileServiceNoFunctions(Vertx vertx) {
        List<FunctionDeployment> functionReservations = new ArrayList<>();
        DockerCredentials credentials = new DockerCredentials();
        credentials.setUsername("user");
        credentials.setAccessToken("access-token");
        Path functionsDir = Paths.get("temp\\test\\functions");
        return new FunctionPrepareService(vertx, functionReservations, functionsDir, credentials);
    }

    public static FunctionPrepareService createFunctionFileServiceFaasVMPython(Vertx vertx) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime);
        Function f2 = TestFunctionProvider.createFunction(2L, "foo2", "false", runtime);
        Resource r1 = TestResourceProvider.createResourceFaaS(1L, region, 250.0, 512.0);
        Resource r2 = TestResourceProvider.createResourceVM(2L, region, "t2.micro");
        return createFunctionFileService(vertx, r1, r2, f1, f2);
    }

    public static FunctionPrepareService createFunctionFileServiceVMEdgePython(Vertx vertx) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime);
        Function f2 = TestFunctionProvider.createFunction(2L, "foo2", "false", runtime);
        Resource r1 = TestResourceProvider.createResourceEdge(1L, "http://localhost:8081", "user1",
            "pw1");
        Resource r2 = TestResourceProvider.createResourceVM(2L, region, "t2.micro");
        return createFunctionFileService(vertx, r1, r2, f1, f2);
    }

    public static FunctionPrepareService createFunctionFileServiceVMEdgeInvalidRuntime(Vertx vertx) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "invalid");
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime);
        Function f2 = TestFunctionProvider.createFunction(2L, "foo2", "false", runtime);
        Resource r1 = TestResourceProvider.createResourceEdge(1L, "http://localhost:8081", "user1",
            "pw1");
        Resource r2 = TestResourceProvider.createResourceVM(2L, region, "t2.micro");
        return createFunctionFileService(vertx, r1, r2, f1, f2);
    }

    public static FunctionPrepareService createFunctionFileServiceFunctionTwicePython(Vertx vertx) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime);
        Resource r1 = TestResourceProvider.createResourceEdge(1L, "http://localhost:8081", "user1",
            "pw1");
        Resource r2 = TestResourceProvider.createResourceVM(2L, region, "t2.micro");
        return createFunctionFileService(vertx, r1, r2, f1, f1);
    }

    public static MainFileService createMainFileService(FileSystem fileSystem, List<TerraformModule> terraformModules) {
        Path rootFolder = Path.of("temp","test");
        return new MainFileService(fileSystem, rootFolder, terraformModules);
    }

    public static ContainerDeployFileService createContainerDeployFileService(FileSystem fileSystem, Path rootFolder,
            Deployment reservation) {
        ServiceDeployment serviceReservation = TestServiceProvider.createServiceReservation(1L, reservation);
        return new ContainerDeployFileService(fileSystem, rootFolder, serviceReservation,
            reservation.getDeploymentId());
    }

    public static ContainerDeployFileService createContainerDeployFileService(FileSystem fileSystem, Path rootFolder,
            Resource resource, Deployment reservation) {
        ServiceDeployment serviceReservation =
            TestServiceProvider.createServiceReservation(1L, resource, reservation);
        return new ContainerDeployFileService(fileSystem, rootFolder, serviceReservation,
            reservation.getDeploymentId());
    }

    public static ContainerPullFileService createContainerPullFileService(FileSystem fileSystem, Path rootFolder,
        Deployment reservation) {
        ServiceDeployment serviceReservation = TestServiceProvider.createServiceReservation(1L, reservation);
        return new ContainerPullFileService(fileSystem, rootFolder, List.of(serviceReservation),
            reservation.getDeploymentId());
    }

    public static ContainerPullFileService createContainerPullFileService(FileSystem fileSystem, Path rootFolder,
        Deployment reservation, List<ServiceDeployment> serviceReservations) {
        return new ContainerPullFileService(fileSystem, rootFolder, serviceReservations,
            reservation.getDeploymentId());
    }
}
