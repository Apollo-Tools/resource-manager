package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.deployment.output.TFOutput;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.ResourceProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDTOProvider {
    public static SLOValue createSLOValue(double value) {
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
        functionsToDeploy.getFunctionsString().append("\"  foo1_python39:\\n    lang: python3-flask-debian\\n    " +
            "handler: ./foo1_python39\\n    image: user/foo1_python39:latest\\n  foo2_python39:\\n    " +
            "lang: python3-flask-debian\\n    handler: ./foo2_python39\\n    image: user/foo2_python39:latest\\n\"");
        return functionsToDeploy;
    }

    public static DeploymentCredentials createDeploymentCredentialsAWSEdge() {
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L);
        DeploymentCredentials deploymentCredentials = new DeploymentCredentials();
        deploymentCredentials.getEdgeLoginCredentials()
            .append("edge_login_data=[{auth_user=\\\"user\\\",auth_pw=\\\"pw\\\"},]");
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
        processOutput.setProcessOutput(output);
        return processOutput;
    }

    public static TFOutput createTFOutputFaas() {
        TFOutput tfOutput = new TFOutput();
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("r1_foo1", "http://localhostfaas1");
        tfOutput.setValue(valueMap);
        return tfOutput;
    }

    public static TFOutput createTFOutputVM() {
        TFOutput tfOutput = new TFOutput();
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("r2_foo1", "http://localhostvm1");
        valueMap.put("r2_foo2", "http://localhostvm2");
        tfOutput.setValue(valueMap);
        return tfOutput;
    }

    public static TFOutput createTFOutputEdge() {
        TFOutput tfOutput = new TFOutput();
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("r3_foo1", "http://localhostedge1");
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
}
