package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.TerminateResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.reservation.ServiceResourceIds;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.model.Runtime;

import java.util.List;

public class TestRequestProvider {

    public static ReserveResourcesRequest createReserveResourcesRequest(List<FunctionResourceIds> functionResources,
            List<ServiceResourceIds> serviceResources, DockerCredentials dockerCredentials) {
        ReserveResourcesRequest request = new ReserveResourcesRequest();
        request.setFunctionResources(functionResources);
        request.setServiceResources(serviceResources);
        request.setDockerCredentials(dockerCredentials);
        request.setKubeConfig(TestDTOProvider.createKubeConfigValue());
        return request;
    }

    public static ReserveResourcesRequest createReserveResourcesRequest(List<FunctionResourceIds> functionResources,
            List<ServiceResourceIds> serviceResources) {
        return createReserveResourcesRequest(functionResources, serviceResources,
            TestDTOProvider.createDockerCredentials());
    }

    public static DeployResourcesRequest createDeployRequest() {
        DeployResourcesRequest deployRequest = new DeployResourcesRequest();
        deployRequest.setReservation(TestReservationProvider.createReservation(1L));
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region);
        deployRequest.setVpcList(List.of(vpc));
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        deployRequest.setDockerCredentials(dockerCredentials);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime);
        Function f2 = TestFunctionProvider.createFunction(2L, "foo2", "false", runtime);
        Resource r1 = TestResourceProvider.createResourceFaaS(1L, region, 250.0, 512.0);
        Resource r2 = TestResourceProvider.createResourceVM(2L, region, "t2.micro");
        Resource r3 = TestResourceProvider.createResourceEdge(3L, "http://localhost:8080",
            "user", "pw");
        Resource r4 = TestResourceProvider.createResourceContainer(3L, "https://localhost", 1, 0.5,
            256);
        FunctionReservation fr1 = TestFunctionProvider.createFunctionReservation(1L, f1, r1);
        FunctionReservation fr2 = TestFunctionProvider.createFunctionReservation(2L, f1, r2);
        FunctionReservation fr3 = TestFunctionProvider.createFunctionReservation(3L, f2, r2);
        FunctionReservation fr4 = TestFunctionProvider.createFunctionReservation(4L, f1, r3);
        List<FunctionReservation> functionReservations = List.of(fr1, fr2, fr3, fr4);
        deployRequest.setFunctionReservations(functionReservations);
        Service s1 = TestServiceProvider.createService(1L, "s1:latest");
        Service s2 = TestServiceProvider.createService(2L, "s2:latest");
        ServiceReservation sr1 = TestServiceProvider.createServiceReservation(5L, s1, r4);
        ServiceReservation sr2 = TestServiceProvider.createServiceReservation(6L, s2, r4);
        List<ServiceReservation> serviceReservations = List.of(sr1, sr2);
        deployRequest.setServiceReservations(serviceReservations);
        Credentials c1 = TestAccountProvider.createCredentials(1L, region.getResourceProvider());
        deployRequest.setCredentialsList(List.of(c1));
        deployRequest.setKubeConfig(TestDTOProvider.createKubeConfigValue());
        return deployRequest;
    }

    public static TerminateResourcesRequest createTerminateRequest() {
        TerminateResourcesRequest terminateRequest = new TerminateResourcesRequest();
        terminateRequest.setReservation(TestReservationProvider.createReservation(1L));
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime);
        Function f2 = TestFunctionProvider.createFunction(2L, "foo2", "false", runtime);
        Resource r1 = TestResourceProvider.createResourceFaaS(1L, region, 250.0, 512.0);
        Resource r2 = TestResourceProvider.createResourceVM(2L, region, "t2.micro");
        Resource r3 = TestResourceProvider.createResourceEdge(3L, "http://localhost:8080", "user", "pw");
        Resource r4 = TestResourceProvider.createResourceContainer(3L, "http://localhost", 1, 0.5,
            256);
        FunctionReservation fr1 = TestFunctionProvider.createFunctionReservation(1L, f1, r1);
        FunctionReservation fr2 = TestFunctionProvider.createFunctionReservation(2L, f1, r2);
        FunctionReservation fr3 = TestFunctionProvider.createFunctionReservation(3L, f2, r2);
        FunctionReservation fr4 = TestFunctionProvider.createFunctionReservation(4L, f1, r3);
        List<FunctionReservation> functionReservations = List.of(fr1, fr2, fr3, fr4);
        terminateRequest.setFunctionReservations(functionReservations);
        Service s1 = TestServiceProvider.createService(1L, "s1:latest");
        Service s2 = TestServiceProvider.createService(2L, "s2:latest");
        ServiceReservation sr1 = TestServiceProvider.createServiceReservation(5L, s1, r4);
        ServiceReservation sr2 = TestServiceProvider.createServiceReservation(6L, s2, r4);
        List<ServiceReservation> serviceReservations = List.of(sr1, sr2);
        terminateRequest.setServiceReservations(serviceReservations);
        Credentials c1 = TestAccountProvider.createCredentials(1L, region.getResourceProvider());
        terminateRequest.setCredentialsList(List.of(c1));
        return terminateRequest;
    }
}
