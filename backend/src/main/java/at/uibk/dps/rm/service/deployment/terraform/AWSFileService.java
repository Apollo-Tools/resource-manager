package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.EC2DeploymentData;
import at.uibk.dps.rm.entity.deployment.LambdaDeploymentData;
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

    private final Set<Long> faasFunctionIds = new HashSet<>();

    private final Set<Long> vmResourceIds = new HashSet<>();

    private final Set<Long> vmFunctionIds = new HashSet<>();

    private final LambdaDeploymentData lambdaDeploymentData;

    private final EC2DeploymentData ec2DeploymentData;

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
            }
        }

        return lambdaDeploymentData.getModuleString() + ec2DeploymentData.getModuleString();
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
            faasFunctionIds.add(function.getFunctionId());
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
        // TODO: swap with check, if vm is already deployed
        String functionIdentifier =  function.getFunctionDeploymentId();
        if (checkMustDeployVM(resource)) {
            String instanceType = metricValues.get("instance-type").getValueString();
            deploymentData.appendValues(resourceName, instanceType, resource.getResourceId(), functionIdentifier);
            vmResourceIds.add(resource.getResourceId());
        } else {
            deploymentData.appendValues(resourceName, resource.getResourceId(), functionIdentifier);
        }
        vmFunctionIds.add(function.getFunctionId());
    }

    @Override
    protected String getVmModulesString() {
        //TODO: remove
        return "";
    }

    /**
     * Check whether a new virtual machine has to deployed or not.
     *
     * @param resource the virtual machine
     * @return true if a new virtua machine has to be deployed, else false
     */
    private boolean checkMustDeployVM(Resource resource) {
        return !vmResourceIds.contains(resource.getResourceId());
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
            "}\n";
    }

    @Override
    protected String getOutputString() {
        StringBuilder outputString = new StringBuilder();
        if (!this.faasFunctionIds.isEmpty()) {
            String functionUrl =
                "output \"function_urls\" {\n" +
                "  value = module.lambda.function_urls\n" +
                "}\n";
            outputString.append(functionUrl);
        }
        if (!this.vmFunctionIds.isEmpty()) {
            StringBuilder vmUrls = new StringBuilder(), vmFunctionIds = new StringBuilder();
            for (FunctionReservation functionReservation: functionReservations) {
                Resource resource = functionReservation.getResource();
                Function function = functionReservation.getFunction();
                String functionIdentifier = function.getFunctionDeploymentId();
                if (resource.getPlatform().getPlatform().equals(PlatformEnum.EC2.getValue())) {
                    vmUrls.append(String.format("module.r%s_%s.function_url,",
                        resource.getResourceId(), functionIdentifier));
                    vmFunctionIds.append(String.format("\"r%s_%s\",",
                        resource.getResourceId(), functionIdentifier));
                }
            }
            outputString.append(String.format(
                "output \"vm_urls\" {\n" +
                "  value = zipmap([%s], [%s])\n" +
                "}\n", vmFunctionIds, vmUrls
            ));

        }
        setModuleResourceTypes();
        return outputString.toString();
    }

    @Override
    protected void setModuleResourceTypes() {
        getModule().setHasFaas(!faasFunctionIds.isEmpty());
        getModule().setHasVM(!vmResourceIds.isEmpty());
    }

    @Override
    protected String getMainFileContent() {
        return this.getProviderString() +
            this.getFunctionsModulString();
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
