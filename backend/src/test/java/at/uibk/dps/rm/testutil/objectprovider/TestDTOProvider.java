package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.ListResourcesBySLOsRequest;
import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.deployment.DeploymentAlertingDTO;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.function.InvocationMonitoringDTO;
import at.uibk.dps.rm.entity.dto.function.InvocationResponseBodyDTO;
import at.uibk.dps.rm.entity.dto.function.InvokeFunctionDTO;
import at.uibk.dps.rm.entity.dto.function.UpdateFunctionDTO;
import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.entity.dto.service.K8sServiceTypeId;
import at.uibk.dps.rm.entity.dto.service.UpdateServiceDTO;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.*;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class to instantiate DTO objects of different types.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestDTOProvider {
    public static SLOValue createSLOValue(Number value) {
        SLOValue sloValue = new SLOValue();
        sloValue.setSloValueType(SLOValueType.NUMBER);
        sloValue.setValueNumber(value);
        return sloValue;
    }

    public static SLOValue createSLOValue(String value) {
        SLOValue sloValue = new SLOValue();
        sloValue.setSloValueType(SLOValueType.STRING);
        sloValue.setValueString(value);
        return sloValue;
    }

    public static SLOValue createSLOValue(boolean value) {
        SLOValue sloValue = new SLOValue();
        sloValue.setSloValueType(SLOValueType.BOOLEAN);
        sloValue.setValueBool(value);
        return sloValue;
    }

    public static ServiceLevelObjective createServiceLevelObjective(String metricName, ExpressionType expressionType,
            List<SLOValue> sloValues) {
        return new ServiceLevelObjective(metricName, expressionType, sloValues);
    }

    public static ServiceLevelObjective createServiceLevelObjective(String metricName, ExpressionType expressionType,
                                                                    double... value) {
        List<SLOValue> sloValues = new ArrayList<>();
        for (double v : value) {
            SLOValue sloValue = createSLOValue(v);
            sloValues.add(sloValue);
        }
        return new ServiceLevelObjective(metricName, expressionType, sloValues);
    }

    public static ServiceLevelObjective createServiceLevelObjective(String metricName, ExpressionType expressionType,
                                                                    String... value) {
        List<SLOValue> sloValues = new ArrayList<>();
        for (String v : value) {
            SLOValue sloValue = createSLOValue(v);
            sloValues.add(sloValue);
        }
        return new ServiceLevelObjective(metricName, expressionType, sloValues);
    }

    public static ServiceLevelObjective createServiceLevelObjective(String metricName, ExpressionType expressionType,
                                                                    boolean... value) {
        List<SLOValue> sloValues = new ArrayList<>();
        for (boolean v : value) {
            SLOValue sloValue = createSLOValue(v);
            sloValues.add(sloValue);
        }
        return new ServiceLevelObjective(metricName, expressionType, sloValues);
    }

    public static DockerCredentials createDockerCredentials(String registry) {
        DockerCredentials dockerCredentials = new DockerCredentials();
        dockerCredentials.setUsername("testuser");
        dockerCredentials.setAccessToken("abcdef12234");
        dockerCredentials.setRegistry(registry);
        return dockerCredentials;
    }

    public static DockerCredentials createDockerCredentials() {
        return createDockerCredentials("docker.io");
    }

    public static FunctionsToDeploy createFunctionsToDeploy() {
        FunctionsToDeploy functionsToDeploy = new FunctionsToDeploy();
        functionsToDeploy.getFunctionIdentifiers().add("foo1_python38");
        functionsToDeploy.getFunctionIdentifiers().add("foo2_python38");
        functionsToDeploy.getDockerFunctionIdentifiers().add("foo1_python38");
        functionsToDeploy.getDockerFunctionIdentifiers().add("foo2_python38");
        functionsToDeploy.setDockerFunctionsString("\"  foo1_python38:\\n    lang: python3-flask-debian\\n    " +
            "handler: ./foo1_python38\\n    image: user/foo1_python38:latest\\n  foo2_python38:\\n    " +
            "lang: python3-flask-debian\\n    handler: ./foo2_python38\\n    image: user/foo2_python38:latest\\n\"");
        return functionsToDeploy;
    }

    public static DeploymentCredentials createDeploymentCredentialsAWSOpenfaas() {
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L);
        DeploymentCredentials deploymentCredentials = new DeploymentCredentials();
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            deploymentCredentials
                .getOpenFaasCredentials().add("r1={auth_user=\\\"user\\\", auth_pw=\\\"pw\\\"}");
        } else {
            deploymentCredentials
                .getOpenFaasCredentials().add("r1={auth_user=\"user\", auth_pw=\"pw\"}");
        }
        deploymentCredentials.getCloudCredentials().add(TestAccountProvider.createCredentials(1L, rp));
        return deploymentCredentials;
    }


    public static DeploymentCredentials createDeploymentCredentialsAWS() {
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L);
        DeploymentCredentials deploymentCredentials = new DeploymentCredentials();
        deploymentCredentials.getCloudCredentials().add(TestAccountProvider.createCredentials(1L, rp));
        return deploymentCredentials;
    }

    public static ProcessOutput createProcessOutput(Process process, String output) {
        ProcessOutput processOutput = new ProcessOutput();
        processOutput.setProcess(process);
        processOutput.setOutput(output);
        return processOutput;
    }

    public static List<SLOValue> createSLOValueList(Number... values) {
        return Arrays.stream(values).map(TestDTOProvider::createSLOValue).collect(Collectors.toList());
    }

    public static List<SLOValue> createSLOValueList(String... values) {
        return Arrays.stream(values).map(TestDTOProvider::createSLOValue).collect(Collectors.toList());
    }

    public static SLORequest createSLORequest(List<ServiceLevelObjective> slos) {
        SLORequest request = new ListResourcesBySLOsRequest();
        request.setEnvironments(List.of(1L));
        request.setResourceTypes(List.of(5L));
        request.setPlatforms(List.of(3L, 4L));
        request.setRegions(List.of(1L, 2L));
        request.setProviders(List.of(5L));
        request.setServiceLevelObjectives(slos);
        return request;
    }

    public static SLORequest createSLORequest() {
        ServiceLevelObjective slo1 = new ServiceLevelObjective("availability", ExpressionType.GT,
                createSLOValueList(0.8));
        return createSLORequest(List.of(slo1));
    }

    public static CreateEnsembleRequest createCreateEnsembleRequest(long... resourceIds) {
        CreateEnsembleRequest request = new CreateEnsembleRequest();
        request.setName("ensemble");
        request.setEnvironments(List.of(1L));
        request.setResourceTypes(List.of(5L));
        request.setPlatforms(List.of(3L, 4L));
        request.setRegions(List.of(1L, 2L));
        request.setProviders(List.of(5L));

        ServiceLevelObjective slo1 = new ServiceLevelObjective("availability", ExpressionType.GT,
                createSLOValueList(0.8));
        request.setServiceLevelObjectives(List.of(slo1));
        List<ResourceId> resourceIdList = new ArrayList<>();
        for (long id : resourceIds) {
            ResourceId resourceId = new ResourceId();
            resourceId.setResourceId(id);
            resourceIdList.add(resourceId);
        }
        request.setResources(resourceIdList);
        return request;
    }

    public static GetOneEnsemble createGetOneEnsemble() {
        GetOneEnsemble getOneEnsemble = new GetOneEnsemble();
        getOneEnsemble.setEnsembleId(1L);
        getOneEnsemble.setName("ensemble");
        getOneEnsemble.setEnvironments(List.of(1L));
        getOneEnsemble.setResourceTypes(List.of(5L));
        getOneEnsemble.setPlatforms(List.of(3L, 4L));
        getOneEnsemble.setRegions(List.of(1L, 2L));
        getOneEnsemble.setProviders(List.of(5L));
        ServiceLevelObjective slo1 = new ServiceLevelObjective("timeout", ExpressionType.GT,
                createSLOValueList(150));
        getOneEnsemble.setServiceLevelObjectives(Stream.of(slo1).collect(Collectors.toList()));
        Region region1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        Region region2 = TestResourceProviderProvider.createRegion(2L, "us-west-1");
        Resource r1 = TestResourceProvider.createResourceFaaS(1L, region1, 200.0, 1024.0);
        Resource r2 = TestResourceProvider.createResourceEC2(2L, region2, "t1.micro");
        getOneEnsemble.setResources(List.of(r1, r2));
        return getOneEnsemble;
    }

    public static UpdateFunctionDTO createUpdateFunctionDTO(String code, boolean isFile, boolean isPublic,
            short timeout, short memory) {
        UpdateFunctionDTO dto = new UpdateFunctionDTO();
        dto.setCode(code);
        dto.setIsFile(isFile);
        dto.setIsPublic(isPublic);
        dto.setTimeoutSeconds(timeout);
        dto.setMemoryMegabytes(memory);
        return dto;
    }

    public static UpdateServiceDTO createUpdateServiceDTO(int replicas, List<String> ports, BigDecimal cpu, int memory,
            K8sServiceTypeId k8sServiceType, List<EnvVar> envVars, List<VolumeMount> volumeMounts, boolean isPublic) {
        UpdateServiceDTO dto = new UpdateServiceDTO();
        dto.setReplicas(replicas);
        dto.setPorts(ports);
        dto.setCpu(cpu);
        dto.setMemory(memory);
        dto.setK8sServiceType(k8sServiceType);
        dto.setEnvVars(envVars);
        dto.setVolumeMounts(volumeMounts);
        dto.setIsPublic(isPublic);
        return dto;
    }

    public static UpdateServiceDTO createUpdateServiceDTO(List<String> ports, List<EnvVar> envVars,
            List<VolumeMount> volumeMounts) {
        K8sServiceTypeId k8sServiceTypeId = new K8sServiceTypeId();
        k8sServiceTypeId.setServiceTypeId(1L);
        return createUpdateServiceDTO(1, ports, BigDecimal.valueOf(13.37), 128, k8sServiceTypeId,
            envVars, volumeMounts, true);
    }

    public static InvocationMonitoringDTO createInvocationMonitoringDTO(double executionTime, long timestamp) {
        InvocationMonitoringDTO invocationMonitoringDTO = new InvocationMonitoringDTO();
        invocationMonitoringDTO.setExecutionTimeMs(executionTime);
        invocationMonitoringDTO.setStartTimestamp(timestamp);
        return invocationMonitoringDTO;
    }

    public static InvocationResponseBodyDTO createInvocationResponseDTO(InvocationMonitoringDTO monitoringData,
                                                                        String body) {
        InvocationResponseBodyDTO invocationResponseDTO = new InvocationResponseBodyDTO();
        invocationResponseDTO.setMonitoringData(monitoringData);
        invocationResponseDTO.setBody(body);
        return invocationResponseDTO;
    }

    public static InvokeFunctionDTO createInvokeFunctionDTO(String body, int statusCode) {
        InvokeFunctionDTO invokeFunctionDTO = new InvokeFunctionDTO();
        invokeFunctionDTO.setBody(body);
        invokeFunctionDTO.setStatusCode(statusCode);
        return invokeFunctionDTO;
    }

    public static DeploymentAlertingDTO createDeploymentAlertingDTO(long deploymentId, long ensembleId,
            List<Resource> resources, List<EnsembleSLO> ensembleSLOS) {
        DeploymentAlertingDTO deploymentAlertingDTO = new DeploymentAlertingDTO();
        deploymentAlertingDTO.setDeploymentId(deploymentId);
        deploymentAlertingDTO.setEnsembleId(ensembleId);
        deploymentAlertingDTO.setResources(resources);
        deploymentAlertingDTO.setEnsembleSLOs(ensembleSLOS);
        deploymentAlertingDTO.setAlertingUrl("http://localhost:9999");
        return deploymentAlertingDTO;
    }
}
