package at.uibk.dps.rm.service.deployment.util;

import at.uibk.dps.rm.entity.deployment.EC2DeploymentData;
import at.uibk.dps.rm.entity.deployment.LambdaDeploymentData;
import at.uibk.dps.rm.entity.deployment.OpenFaasDeploymentData;
import at.uibk.dps.rm.entity.dto.resource.RuntimeEnum;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.RuntimeNotSupportedException;
import at.uibk.dps.rm.util.misc.MetricValueMapper;
import lombok.experimental.UtilityClass;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This utility class is used to compose the deployment data for different deployment platforms.
 *
 * @author matthi-g
 */
@UtilityClass
public class ComposeDeploymentDataUtility {

    /**
     * Compose and store the deployment data for AWS Lambda to deploymentData.
     *
     * @param resource the resource of the deployment
     * @param function the function of the deployment
     * @param deploymentId the id of the deployment
     * @param functionsDir the functions directory for the deployment
     * @param deploymentData the object where the composed data is stored
     */
    public static void composeLambdaDeploymentData(Resource resource, Function function, long deploymentId,
            Path functionsDir, LambdaDeploymentData deploymentData) {
        StringBuilder functionName = new StringBuilder(), functionPath = new StringBuilder();
        RuntimeEnum runtime = RuntimeEnum.fromRuntime(function.getRuntime());
        String functionIdentifier =  function.getFunctionDeploymentId();
        String functionHandler;
        String layer = "\"\"";
        functionName.append("r").append(resource.getResourceId()).append("_")
            .append(functionIdentifier).append("_").append(deploymentId);
        functionPath.append(functionsDir.toAbsolutePath().toString().replace("\\","/")).append("/")
            .append(functionIdentifier).append(".zip");
        switch (runtime) {
            case PYTHON38:
                functionHandler = "lambda.handler";
                layer = function.getIsFile() ? "\"python38\"" : layer;
                break;
            case JAVA11:
                functionHandler = "org.apollorm.entrypoint.App";
                break;
            default:
                throw new RuntimeNotSupportedException();
        }
        Map<String, MetricValue> metricValues = MetricValueMapper.mapMetricValues(resource.getMetricValues());
        String deploymentRole = metricValues.get("deployment-role").getValueString();
        deploymentData.appendValues(functionName.toString(), functionPath.toString(), functionHandler,
            function.getTimeoutSeconds(), function.getMemoryMegabytes(), layer, runtime.getValue(), deploymentRole);
    }

    /**
     * Compose and store the deployment data for AWS EC2 to deploymentData.
     *
     * @param resource the resource of the deployment
     * @param function the function of the deployment
     * @param deploymentData the object where the composed data is stored
     */
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
            deploymentData.appendValues(resourceName, instanceType, resource.getResourceId(), functionIdentifier,
                function.getTimeoutSeconds());
        } else {
            deploymentData.appendValues(resourceName, resource.getResourceId(), functionIdentifier,
                function.getTimeoutSeconds());
        }
    }

    /**
     * Compose and store the deployment data for OpenFaaS to deploymentData.
     *
     * @param resource the resource of the deployment
     * @param function the function of the deployment
     * @param deploymentData the object where the composed data is stored
     */
    public static void composeOpenFassDeploymentData(Resource resource, Function function,
            OpenFaasDeploymentData deploymentData) {
        String functionIdentifier =  function.getFunctionDeploymentId();
        Map<String, MetricValue> metricValues = MetricValueMapper.mapMetricValues(resource.getMetricValues());
        String baseUrl = metricValues.get("base-url").getValueString();
        int metricsPort = metricValues.get("metrics-port").getValueNumber().intValue();
        int openFaasPort = metricValues.get("openfaas-port").getValueNumber().intValue();
        deploymentData.appendValues(resource.getResourceId(), functionIdentifier, baseUrl, metricsPort, openFaasPort,
            function.getTimeoutSeconds());
    }

    /**
     * Check whether a new virtual machine has to be deployed or not.
     *
     * @param resource the virtual machine
     * @return true if a new virtual machine has to be deployed, else false
     */
    private static boolean checkMustDeployVM(Resource resource, EC2DeploymentData deploymentData) {
        return !deploymentData.getResourceIds().contains(resource.getResourceId());
    }
}
