package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.EC2DeploymentData;
import at.uibk.dps.rm.entity.deployment.LambdaDeploymentData;
import at.uibk.dps.rm.entity.deployment.OpenFaasDeploymentData;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.deployment.TerraformModule;
import at.uibk.dps.rm.exception.RuntimeNotSupportedException;
import at.uibk.dps.rm.util.misc.MetricValueMapper;
import io.vertx.rxjava3.core.file.FileSystem;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Extension of the #ModuleFileService to set up an AWS module of a deployment.
 *
 * @author matthi-g
 */
public class AWSFileService extends ModuleFileService {

    private final Path functionsDir;

    private final Region region;

    private final List<FunctionReservation> functionReservations;

    private final long reservationId;

    private final LambdaDeploymentData lambdaDeploymentData;

    private final EC2DeploymentData ec2DeploymentData;

    private final OpenFaasDeploymentData openFaasDeploymentData;

    /**
     * Create an instance from the fileSystem, rootFolder, functionsDir, region, awsRole,
     * functionReservations, reservationId, module, dockerUserName and vpc.
     *
     * @param fileSystem the vertx file system
     * @param rootFolder the root folder of the module
     * @param functionsDir the path to the packaged functions
     * @param region the region where the resources are deployed
     * @param awsRole the aws role to use
     * @param functionReservations the list of function reservations
     * @param reservationId the id of the reservation
     * @param module the terraform module
     * @param dockerUserName the docker username
     * @param vpc the virtual private cloud to use for the deployment
     */
    public AWSFileService(FileSystem fileSystem, Path rootFolder, Path functionsDir, Region region, String awsRole,
                          List<FunctionReservation> functionReservations, long reservationId, TerraformModule module,
                          String dockerUserName, VPC vpc) {
        super(fileSystem, rootFolder, module);
        this.functionsDir = functionsDir;
        this.region = region;
        this.functionReservations = functionReservations;
        this.reservationId = reservationId;
        this.lambdaDeploymentData = new LambdaDeploymentData();
        this.lambdaDeploymentData.setAwsRole(awsRole);
        this.ec2DeploymentData = new EC2DeploymentData(reservationId, vpc, dockerUserName);
        this.openFaasDeploymentData = new OpenFaasDeploymentData(reservationId, dockerUserName);
    }


    @Override
    protected String getProviderString() {
        return String.format(
            "provider \"aws\" {\n" +
                "  access_key = var.access_key\n" +
                "  secret_key = var.secret_access_key\n" +
                "  token = var.session_token\n" +
                "  region = \"%s\"\n" +
                "}\n", region.getName());
    }

    @Override
    protected String getFunctionsModulString() {
        for (FunctionReservation functionReservation: functionReservations) {
            Resource resource = functionReservation.getResource();
            Function function = functionReservation.getFunction();
            PlatformEnum platform = PlatformEnum.fromString(resource.getPlatform().getPlatform());
            switch (platform) {
                case LAMBDA:
                    composeLambdaDeploymentData(resource, function, lambdaDeploymentData);
                    break;
                case EC2:
                    composeEC2DeploymentData(resource, function, ec2DeploymentData);
                    break;
                case OPENFAAS:
                    composeOpenFassDeploymentData(resource, function, openFaasDeploymentData);
                    break;
            }
        }

        return lambdaDeploymentData.getModuleString() + ec2DeploymentData.getModuleString() +
            openFaasDeploymentData.getModuleString();
    }

    private void composeLambdaDeploymentData(Resource resource, Function function,
            LambdaDeploymentData deploymentData) {
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
        deploymentData.appendValues(functionName.toString(), functionPath.toString(), functionHandler, timeout,
            memorySize, "[]", runtime);
    }

    private void composeEC2DeploymentData(Resource resource, Function function, EC2DeploymentData deploymentData) {
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

    private void composeOpenFassDeploymentData(Resource resource, Function function,
            OpenFaasDeploymentData deploymentData) {
        String functionIdentifier =  function.getFunctionDeploymentId();
        Map<String, MetricValue> metricValues = MetricValueMapper.mapMetricValues(resource.getMetricValues());
        String gatewayUrl = metricValues.get("gateway-url").getValueString();
        deploymentData.appendValues(resource.getResourceId(), functionIdentifier, gatewayUrl);
    }

    @Override
    protected String getVmModulesString() {
        //TODO: remove
        return "";
    }

    /**
     * Check whether a new virtual machine has to be deployed or not.
     *
     * @param resource the virtual machine
     * @return true if a new virtua machine has to be deployed, else false
     */
    private boolean checkMustDeployVM(Resource resource, EC2DeploymentData deploymentData) {
        return !deploymentData.getResourceIds().contains(resource.getResourceId());
    }

    @Override
    protected String getCredentialVariablesString() {
        return "variable \"access_key\" {\n" +
            "  type = string\n" +
            "  default = \"\"\n" +
            "}\n" +
            "variable \"secret_access_key\" {\n" +
            "  type = string\n" +
            "  default = \"\"\n" +
            "}\n" +
            "variable \"session_token\" {\n" +
            "  type = string\n" +
            "  default = \"\"\n" +
            "}\n" +
            "variable \"openfaas_login_data\" {\n" +
            "  type = map(object({\n" +
            "      auth_user = string\n" +
            "      auth_pw = string\n" +
            "  }))\n" +
            "  default = {}\n" +
            "}\n";
    }

    @Override
    protected String getOutputString() {
        StringBuilder outputString = new StringBuilder();
        String lambdaUrls = "{}", openFaasUrls = "{}";
        if (this.lambdaDeploymentData.getFunctionCount() > 0) {
            lambdaUrls = "module.lambda.function_urls";
        }
        if (this.ec2DeploymentData.getFunctionCount() > 0 || this.openFaasDeploymentData.getFunctionCount() > 0) {
            StringBuilder vmUrls = new StringBuilder(), vmFunctionIds = new StringBuilder();
            for (FunctionReservation functionReservation: functionReservations) {
                Resource resource = functionReservation.getResource();
                Function function = functionReservation.getFunction();
                String functionIdentifier = function.getFunctionDeploymentId();
                PlatformEnum platformEnum = PlatformEnum.fromString(resource.getPlatform().getPlatform());
                if (platformEnum.equals(PlatformEnum.EC2) || platformEnum.equals(PlatformEnum.OPENFAAS)) {
                    vmUrls.append(String.format("module.r%s_%s.function_url,",
                        resource.getResourceId(), functionIdentifier));
                    vmFunctionIds.append(String.format("\"r%s_%s_%s\",",
                        resource.getResourceId(), functionIdentifier, reservationId));
                }
            }
            openFaasUrls = String.format("zipmap([%s], [%s])", vmFunctionIds, vmUrls);
        }
        outputString.append(String.format(
            "output \"temp\" {\n" +
            "  value = merge(%s, %s)\n" +
            "}\n", lambdaUrls, openFaasUrls
        ));
        setModuleResourceTypes();
        return outputString.toString();
    }

    @Override
    protected void setModuleResourceTypes() {
        getModule().setHasFaas(lambdaDeploymentData.getFunctionCount() > 0);
        getModule().setHasVM(ec2DeploymentData.getFunctionCount() > 0);
    }

    @Override
    protected String getMainFileContent() {
        return this.getProviderString() + this.getFunctionsModulString();
    }

    @Override
    protected String getVariablesFileContent() {
        return this.getCredentialVariablesString();
    }

    @Override
    protected String getOutputsFileContent() {
        return this.getOutputString();
    }
}
