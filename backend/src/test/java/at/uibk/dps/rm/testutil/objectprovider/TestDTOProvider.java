package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.deployment.output.TFOutput;
import at.uibk.dps.rm.entity.dto.ListResourcesBySLOsRequest;
import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceProvider;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static DockerCredentials createDockerCredentials() {
        DockerCredentials dockerCredentials = new DockerCredentials();
        dockerCredentials.setUsername("testuser");
        dockerCredentials.setAccessToken("abcdef12234");
        return dockerCredentials;
    }

    public static FunctionsToDeploy createFunctionsToDeploy() {
        FunctionsToDeploy functionsToDeploy = new FunctionsToDeploy();
        functionsToDeploy.getFunctionIdentifiers().add("foo1_python39");
        functionsToDeploy.getFunctionIdentifiers().add("foo2_python39");
        functionsToDeploy.setFunctionsString("\"  foo1_python39:\\n    lang: python3-flask-debian\\n    " +
            "handler: ./foo1_python39\\n    image: user/foo1_python39:latest\\n  foo2_python39:\\n    " +
            "lang: python3-flask-debian\\n    handler: ./foo2_python39\\n    image: user/foo2_python39:latest\\n\"");
        return functionsToDeploy;
    }

    public static DeploymentCredentials createDeploymentCredentialsAWSEdge() {
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L);
        DeploymentCredentials deploymentCredentials = new DeploymentCredentials();
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            deploymentCredentials
                .setEdgeLoginCredentials("edge_login_data=[{auth_user=\\\"user\\\",auth_pw=\\\"pw\\\"},]");
        } else {
            deploymentCredentials
                .setEdgeLoginCredentials("edge_login_data=[{auth_user=\"user\",auth_pw=\"pw\"},]");
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

    public static TFOutput createEmptyTFOutput() {
        TFOutput tfOutput = new TFOutput();
        tfOutput.setValue(new HashMap<>());
        return  tfOutput;
    }

    public static TFOutput createTFOutputFaas() {
        return createTFOutputFaas("python39");
    }

    public static TFOutput createTFOutputFaas(String runtime) {
        TFOutput tfOutput = new TFOutput();
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("r1_foo1_" + runtime, "http://localhostfaas1");
        tfOutput.setValue(valueMap);
        return tfOutput;
    }

    public static TFOutput createTFOutputVM() {
        TFOutput tfOutput = new TFOutput();
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("r2_foo1_python39", "http://localhostvm1");
        valueMap.put("r2_foo2_python39", "http://localhostvm2");
        tfOutput.setValue(valueMap);
        return tfOutput;
    }

    public static TFOutput createTFOutputEdge() {
        TFOutput tfOutput = new TFOutput();
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("r3_foo1_python39", "http://localhostedge1");
        tfOutput.setValue(valueMap);
        return tfOutput;
    }

    public static DeploymentOutput createDeploymentOutput() {
        DeploymentOutput deploymentOutput = new DeploymentOutput();
        deploymentOutput.setEdgeUrls(createTFOutputEdge());
        deploymentOutput.setVmUrls(createTFOutputVM());
        deploymentOutput.setFunctionUrls(createTFOutputFaas());
        return deploymentOutput;
    }

    public static DeploymentOutput createDeploymentOutputUnknownFunction() {
        DeploymentOutput deploymentOutput = new DeploymentOutput();
        deploymentOutput.setFunctionUrls(createTFOutputFaas("python10"));
        deploymentOutput.setEdgeUrls(createEmptyTFOutput());
        deploymentOutput.setVmUrls(createEmptyTFOutput());
        return deploymentOutput;
    }

    public static List<SLOValue> createSLOValueList(Number... values) {
        return Arrays.stream(values).map(TestDTOProvider::createSLOValue).collect(Collectors.toList());
    }

    public static SLORequest createSLORequest() {
        SLORequest request = new ListResourcesBySLOsRequest();
        request.setProviders(List.of(1L, 2L));
        request.setRegions(List.of(3L, 4L));
        request.setResourceTypes(List.of(5L));
        ServiceLevelObjective slo1 = new ServiceLevelObjective("availability", ExpressionType.GT,
                createSLOValueList(0.8));
        request.setServiceLevelObjectives(List.of(slo1));
        return request;
    }

    public static GetOneEnsemble createGetOneEnsemble() {
        GetOneEnsemble getOneEnsemble = new GetOneEnsemble();
        getOneEnsemble.setEnsembleId(1L);
        getOneEnsemble.setName("ensemble");
        getOneEnsemble.setProviders(List.of(1L, 2L));
        getOneEnsemble.setRegions(List.of(3L, 4L));
        getOneEnsemble.setResourceTypes(List.of(5L));
        ServiceLevelObjective slo1 = new ServiceLevelObjective("availability", ExpressionType.GT,
                createSLOValueList(0.8));
        getOneEnsemble.setServiceLevelObjectives(Stream.of(slo1).collect(Collectors.toList()));
        Resource r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);
        getOneEnsemble.setResources(List.of(r1, r2));
        return getOneEnsemble;
    }
}
