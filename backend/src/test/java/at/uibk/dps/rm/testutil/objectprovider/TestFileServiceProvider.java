package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.deployment.TerraformModule;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.service.deployment.terraform.AWSFileService;
import at.uibk.dps.rm.service.deployment.terraform.EdgeFileService;
import at.uibk.dps.rm.service.deployment.terraform.FunctionFileService;
import at.uibk.dps.rm.service.deployment.terraform.MainFileService;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TestFileServiceProvider {
    public static AWSFileService createAWSFileService(FileSystem fileSystem, Resource r1, Resource r2, Resource r3,
                                                      Runtime runtime, Region region) {
        Path rootFolder = Paths.get("temp\\test");
        Path functionsDir = Path.of(String.valueOf(rootFolder), "functions");
        String awsRole = "LabRole";
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime);
        Function f2 = TestFunctionProvider.createFunction(2L, "foo2", "false", runtime);
        FunctionReservation fr1 = TestFunctionProvider.createFunctionReservation(1L, f1, r1);
        FunctionReservation fr2 = TestFunctionProvider.createFunctionReservation(2L, f1, r2);
        FunctionReservation fr3 = TestFunctionProvider.createFunctionReservation(3L, f2, r2);
        FunctionReservation fr4 = TestFunctionProvider.createFunctionReservation(4L, f1, r3);
        List<FunctionReservation> functionReservations = List.of(fr1, fr2, fr3, fr4);
        long reservationId = 1L;
        TerraformModule module = new TerraformModule(CloudProvider.AWS, region.getResourceProvider()
            .getProvider() + "_" + region.getName().replace("-", "_"));
        String dockerUserName = "dockerUser";
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region);
        return new AWSFileService(fileSystem, rootFolder, functionsDir, region, awsRole,
            functionReservations, reservationId, module, dockerUserName, vpc);
    }

    public static AWSFileService createAWSFileServiceFaasVMEdge(FileSystem fileSystem, Runtime runtime) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Resource r1 = TestResourceProvider.createResourceFaaS(1L, region, 250.0, 512.0);
        Resource r2 = TestResourceProvider.createResourceVM(2L, region, "t2.micro");
        Resource r3 = TestResourceProvider.createResourceEdge(3L, "http://localhost:8080", "user", "pw");
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
        Resource r1 = TestResourceProvider.createResourceEdge(1L, "http://localhost:8080", "user", "pw");
        Resource r2 = TestResourceProvider.createResourceFaaS(1L, region, 250.0, 512.0);
        Resource r3 = TestResourceProvider.createResourceEdge(3L, "http://localhost:8082", "user", "pw");
        return createAWSFileService(fileSystem, r1, r2, r3, runtime, region);
    }

    public static AWSFileService createAWSFileServiceVMEdge(FileSystem fileSystem) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        Resource r1 = TestResourceProvider.createResourceEdge(1L, "http://localhost:8080", "user", "pw");
        Resource r2 = TestResourceProvider.createResourceVM(2L, region, "t2.micro");
        Resource r3 = TestResourceProvider.createResourceVM(3L, region, "t3.micro");
        return createAWSFileService(fileSystem, r1, r2, r3, runtime, region);
    }

    public static AWSFileService createAWSFileServiceEdge(FileSystem fileSystem) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        Resource r1 = TestResourceProvider.createResourceEdge(1L, "http://localhost:8081", "user1", "pw1");
        Resource r2 = TestResourceProvider.createResourceEdge(2L, "http://localhost:8082", "user2", "pw2");
        Resource r3 = TestResourceProvider.createResourceEdge(3L, "http://localhost:8083", "user3", "pw3");
        return createAWSFileService(fileSystem, r1, r2, r3, runtime, region);
    }

    public static EdgeFileService createEdgeFileService(FileSystem fileSystem, Resource r1, Resource r2, Resource r3,
                                                        Runtime runtime) {
        Path rootFolder = Paths.get("temp\\test");
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime);
        Function f2 = TestFunctionProvider.createFunction(2L, "foo2", "false", runtime);
        FunctionReservation fr1 = TestFunctionProvider.createFunctionReservation(1L, f1, r1);
        FunctionReservation fr2 = TestFunctionProvider.createFunctionReservation(2L, f1, r2);
        FunctionReservation fr3 = TestFunctionProvider.createFunctionReservation(3L, f2, r2);
        FunctionReservation fr4 = TestFunctionProvider.createFunctionReservation(4L, f1, r3);
        List<FunctionReservation> functionReservations = List.of(fr1, fr2, fr3, fr4);
        long reservationId = 1L;
        String dockerUserName = "dockerUser";
        return new EdgeFileService(fileSystem, rootFolder, functionReservations, reservationId, dockerUserName);
    }

    public static EdgeFileService createEdgeFileServiceEdge(FileSystem fileSystem) {
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        Resource r1 = TestResourceProvider.createResourceEdge(1L, "http://localhost:8081", "user1", "pw1");
        Resource r2 = TestResourceProvider.createResourceEdge(2L, "http://localhost:8082", "user2", "pw2");
        Resource r3 = TestResourceProvider.createResourceEdge(3L, "http://localhost:8083", "user3", "pw3");
        return createEdgeFileService(fileSystem, r1, r2, r3, runtime);
    }

    public static EdgeFileService createEdgeFileServiceFaasVM(FileSystem fileSystem) {
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Resource r1 = TestResourceProvider.createResourceFaaS(1L, region, 250.0, 512.0);
        Resource r2 = TestResourceProvider.createResourceVM(2L, region, "t2.micro");
        Resource r3 = TestResourceProvider.createResourceVM(3L, region, "t2.large");
        return createEdgeFileService(fileSystem, r1, r2, r3, runtime);
    }

    public static FunctionFileService createFunctionFileService(Vertx vertx, Resource r1, Resource r2, Function f1,
                                                                Function f2) {
        FunctionReservation fr1 = TestFunctionProvider.createFunctionReservation(1L, f1, r1);
        FunctionReservation fr2 = TestFunctionProvider.createFunctionReservation(2L, f2, r2);
        List<FunctionReservation> functionReservations = List.of(fr1, fr2);
        DockerCredentials credentials = new DockerCredentials();
        credentials.setUsername("user");
        credentials.setAccessToken("access-token");
        Path functionsDir = Paths.get("temp\\test\\functions");
        return new FunctionFileService(vertx, functionReservations, functionsDir, credentials);
    }

    public static FunctionFileService createFunctionFileServiceNoFunctions(Vertx vertx) {
        List<FunctionReservation> functionReservations = new ArrayList<>();
        DockerCredentials credentials = new DockerCredentials();
        credentials.setUsername("user");
        credentials.setAccessToken("access-token");
        Path functionsDir = Paths.get("temp\\test\\functions");
        return new FunctionFileService(vertx, functionReservations, functionsDir, credentials);
    }

    public static FunctionFileService createFunctionFileServiceFaasVMPython(Vertx vertx) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime);
        Function f2 = TestFunctionProvider.createFunction(2L, "foo2", "false", runtime);
        Resource r1 = TestResourceProvider.createResourceFaaS(1L, region, 250.0, 512.0);
        Resource r2 = TestResourceProvider.createResourceVM(2L, region, "t2.micro");
        return createFunctionFileService(vertx, r1, r2, f1, f2);
    }

    public static FunctionFileService createFunctionFileServiceVMEdgePython(Vertx vertx) {
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

    public static FunctionFileService createFunctionFileServiceVMEdgeInvalidRuntime(Vertx vertx) {
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

    public static FunctionFileService createFunctionFileServiceFunctionTwicePython(Vertx vertx) {
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
}
