package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.TerminateResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.model.Runtime;

import java.util.List;

public class TestRequestProvider {

    public static ReserveResourcesRequest createReserveResourcesRequest(List<FunctionResourceIds> functionResources,
                                                                        DockerCredentials dockerCredentials) {
        ReserveResourcesRequest request = new ReserveResourcesRequest();
        request.setFunctionResources(functionResources);
        request.setDockerCredentials(dockerCredentials);
        return request;
    }

    public static ReserveResourcesRequest createReserveResourcesRequest(List<FunctionResourceIds> functionResources) {
        return createReserveResourcesRequest(functionResources, TestDTOProvider.createDockerCredentials());
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
        Resource r3 = TestResourceProvider.createResourceEdge(3L, "http://localhost:8080", "user", "pw");
        FunctionResource fr1 = TestFunctionProvider.createFunctionResource(1L, f1, r1);
        FunctionResource fr2 = TestFunctionProvider.createFunctionResource(2L, f1, r2);
        FunctionResource fr3 = TestFunctionProvider.createFunctionResource(3L, f2, r2);
        FunctionResource fr4 = TestFunctionProvider.createFunctionResource(4L, f1, r3);
        List<FunctionResource> functionResources = List.of(fr1, fr2, fr3, fr4);
        deployRequest.setFunctionResources(functionResources);
        Credentials c1 = TestAccountProvider.createCredentials(1L, region.getResourceProvider());
        deployRequest.setCredentialsList(List.of(c1));
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
        FunctionResource fr1 = TestFunctionProvider.createFunctionResource(1L, f1, r1);
        FunctionResource fr2 = TestFunctionProvider.createFunctionResource(2L, f1, r2);
        FunctionResource fr3 = TestFunctionProvider.createFunctionResource(3L, f2, r2);
        FunctionResource fr4 = TestFunctionProvider.createFunctionResource(4L, f1, r3);
        List<FunctionResource> functionResources = List.of(fr1, fr2, fr3, fr4);
        terminateRequest.setFunctionResources(functionResources);
        Credentials c1 = TestAccountProvider.createCredentials(1L, region.getResourceProvider());
        terminateRequest.setCredentialsList(List.of(c1));
        return terminateRequest;
    }
}
