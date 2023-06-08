package at.uibk.dps.rm.service.deployment.util;

import at.uibk.dps.rm.entity.deployment.EC2DeploymentData;
import at.uibk.dps.rm.entity.deployment.LambdaDeploymentData;
import at.uibk.dps.rm.entity.deployment.OpenFaasDeploymentData;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.RuntimeNotSupportedException;
import at.uibk.dps.rm.util.misc.MetricValueMapper;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class ComposeDeploymentDataUtility {

    public static void composeLambdaDeploymentData(Resource resource, Function function, long reservationId,
            Path functionsDir, LambdaDeploymentData deploymentData) {
        StringBuilder functionName = new StringBuilder(), functionPath = new StringBuilder();
        String runtime = function.getRuntime().getName();
        String functionIdentifier =  function.getFunctionDeploymentId();
        String functionHandler;
        functionName.append("r").append(resource.getResourceId()).append("_")
            .append(functionIdentifier).append("_").append(reservationId);
        functionPath.append(functionsDir.toAbsolutePath().toString().replace("\\","/")).append("/")
            .append(functionIdentifier).append(".zip");
        if (runtime.startsWith("python")) {
            functionHandler = "main.handler";
        } else {
            throw new RuntimeNotSupportedException();
        }
        Map<String, MetricValue> metricValues = MetricValueMapper.mapMetricValues(resource.getMetricValues());
        BigDecimal timeout =  metricValues.get("timeout").getValueNumber();
        BigDecimal memorySize = metricValues.get("memory-size").getValueNumber();
        String deploymentRole = metricValues.get("deployment-role").getValueString();
        deploymentData.appendValues(functionName.toString(), functionPath.toString(), functionHandler, timeout,
            memorySize, "[]", runtime, deploymentRole);
    }

    public static void composeEC2DeploymentData(Resource resource, Function function, EC2DeploymentData deploymentData) {
        String resourceName = "resource_" + resource.getResourceId();
        Map<String, MetricValue> metricValues = resource.getMetricValues()
            .stream()
            .collect(Collectors.toMap(metricValue -> metricValue.getMetric().getMetric(),
                metricValue -> metricValue));
        String functionIdentifier =  function.getFunctionDeploymentId();
        // TODO: swap with check, if vm is already deployed
        if (checkMustDeployVM(resource, deploymentData)) {
            String instanceType = metricValues.get("instance-type").getValueString();
            deploymentData.appendValues(resourceName, instanceType, resource.getResourceId(), functionIdentifier);
        } else {
            deploymentData.appendValues(resourceName, resource.getResourceId(), functionIdentifier);
        }
    }

    public static void composeOpenFassDeploymentData(Resource resource, Function function,
        OpenFaasDeploymentData deploymentData) {
        String functionIdentifier =  function.getFunctionDeploymentId();
        Map<String, MetricValue> metricValues = MetricValueMapper.mapMetricValues(resource.getMetricValues());
        String gatewayUrl = metricValues.get("gateway-url").getValueString();
        deploymentData.appendValues(resource.getResourceId(), functionIdentifier, gatewayUrl);
    }

    /**
     * Check whether a new virtual machine has to be deployed or not.
     *
     * @param resource the virtual machine
     * @return true if a new virtua machine has to be deployed, else false
     */
    private static boolean checkMustDeployVM(Resource resource, EC2DeploymentData deploymentData) {
        return !deploymentData.getResourceIds().contains(resource.getResourceId());
    }
}
