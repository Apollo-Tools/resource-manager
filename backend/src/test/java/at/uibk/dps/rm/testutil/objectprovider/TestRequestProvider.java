package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.credentials.DeploymentCredentials;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDTO;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.model.Runtime;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * Utility class to instantiate objects that are different types of requests.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestRequestProvider {

    public static DeployResourcesRequest createDeployResourcesRequest(List<FunctionResourceIds> functionResources,
            List<ServiceResourceIds> serviceResources, List<ResourceId> lockResources,
            DockerCredentials dockerCredentials, String kubeConfig) {
        DeployResourcesRequest request = new DeployResourcesRequest();
        request.setFunctionResources(functionResources);
        request.setServiceResources(serviceResources);
        DeploymentCredentials deploymentCredentials = new DeploymentCredentials();
        deploymentCredentials.setDockerCredentials(List.of(dockerCredentials));
        deploymentCredentials.setKubeConfig(kubeConfig);
        request.setCredentials(deploymentCredentials);
        request.setLockResources(lockResources);
        return request;
    }

    public static DeployResourcesRequest createDeployResourcesRequest(List<FunctionResourceIds> functionResources,
            List<ServiceResourceIds> serviceResources, List<ResourceId> lockResources,
            DockerCredentials dockerCredentials) {
        return createDeployResourcesRequest(functionResources, serviceResources, lockResources, dockerCredentials,
            TestDTOProvider.createKubeConfigValue());
    }

    public static DeployResourcesRequest createDeployResourcesRequest(List<FunctionResourceIds> functionResources,
            List<ServiceResourceIds> serviceResources, List<ResourceId> lockResources) {
        return createDeployResourcesRequest(functionResources, serviceResources, lockResources,
            TestDTOProvider.createDockerCredentials());
    }

    public static DeployResourcesDTO createDeployRequest() {
        DeployResourcesDTO deployRequest = new DeployResourcesDTO();
        deployRequest.setDeployment(TestDeploymentProvider.createDeployment(1L));
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region);
        deployRequest.setVpcList(List.of(vpc));
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        DeploymentCredentials deploymentCredentials = new DeploymentCredentials();
        deploymentCredentials.setDockerCredentials(List.of(dockerCredentials));
        deploymentCredentials.setKubeConfig(TestDTOProvider.createKubeConfigValue());
        deployRequest.setDeploymentCredentials(deploymentCredentials);
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime, false);
        Function f2 = TestFunctionProvider.createFunction(2L, "foo2", "false", runtime, false);
        Resource r1 = TestResourceProvider.createResourceLambda(1L, region,250.0, 612.0);
        Resource r2 = TestResourceProvider.createResourceEC2(2L, region, 150.0, 512.0,
            "t2.micro");
        Resource r3 = TestResourceProvider.createResourceOpenFaas(3L, region,250.0, 512.0,
            "http://localhost:8080", "user", "pw");
        Resource r4 = TestResourceProvider.createResourceContainer(3L, "https://localhost", true);
        FunctionDeployment fd1 = TestFunctionProvider.createFunctionDeployment(1L, f1, r1);
        FunctionDeployment fd2 = TestFunctionProvider.createFunctionDeployment(2L, f1, r2);
        FunctionDeployment fd3 = TestFunctionProvider.createFunctionDeployment(3L, f2, r2);
        FunctionDeployment fd4 = TestFunctionProvider.createFunctionDeployment(4L, f1, r3);
        List<FunctionDeployment> functionDeployments = List.of(fd1, fd2, fd3, fd4);
        deployRequest.setFunctionDeployments(functionDeployments);
        Service s1 = TestServiceProvider.createService(1L, "s1");
        Service s2 = TestServiceProvider.createService(2L, "s2");
        ServiceDeployment sd1 = TestServiceProvider.createServiceDeployment(5L, s1, r4);
        ServiceDeployment sd2 = TestServiceProvider.createServiceDeployment(6L, s2, r4);
        List<ServiceDeployment> serviceDeployments = List.of(sd1, sd2);
        deployRequest.setServiceDeployments(serviceDeployments);
        Credentials c1 = TestAccountProvider.createCredentials(1L, region.getResourceProvider());
        deployRequest.setCredentialsList(List.of(c1));
        return deployRequest;
    }

    public static TerminateResourcesDTO createTerminateRequest() {
        TerminateResourcesDTO terminateRequest = new TerminateResourcesDTO();
        terminateRequest.setDeployment(TestDeploymentProvider.createDeployment(1L));
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python39");
        Function f1 = TestFunctionProvider.createFunction(1L, "foo1", "true", runtime, false);
        Function f2 = TestFunctionProvider.createFunction(2L, "foo2", "false", runtime, false);
        Resource r1 = TestResourceProvider.createResourceLambda(1L, region, 250.0, 512.0);
        Resource r2 = TestResourceProvider.createResourceEC2(2L, region, 100.0, 1024.0, "t2.micro");
        Resource r3 = TestResourceProvider.createResourceOpenFaas(3L, 100.0, 512.0, "http://localhost:8080", "user",
            "pw");
        Resource r4 = TestResourceProvider.createResourceContainer(3L, "http://localhost", true);
        FunctionDeployment fr1 = TestFunctionProvider.createFunctionDeployment(1L, f1, r1);
        FunctionDeployment fr2 = TestFunctionProvider.createFunctionDeployment(2L, f1, r2);
        FunctionDeployment fr3 = TestFunctionProvider.createFunctionDeployment(3L, f2, r2);
        FunctionDeployment fr4 = TestFunctionProvider.createFunctionDeployment(4L, f1, r3);
        List<FunctionDeployment> functionDeployments = List.of(fr1, fr2, fr3, fr4);
        terminateRequest.setFunctionDeployments(functionDeployments);
        Service s1 = TestServiceProvider.createService(1L, "s1");
        Service s2 = TestServiceProvider.createService(2L, "s2");
        ServiceDeployment sr1 = TestServiceProvider.createServiceDeployment(5L, s1, r4);
        ServiceDeployment sr2 = TestServiceProvider.createServiceDeployment(6L, s2, r4);
        List<ServiceDeployment> serviceDeployments = List.of(sr1, sr2);
        terminateRequest.setServiceDeployments(serviceDeployments);
        Credentials c1 = TestAccountProvider.createCredentials(1L, region.getResourceProvider());
        terminateRequest.setCredentialsList(List.of(c1));
        return terminateRequest;
    }
}
