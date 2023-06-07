package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.deployment.TerraformModule;
import at.uibk.dps.rm.exception.RuntimeNotSupportedException;
import at.uibk.dps.rm.util.misc.MetricValueMapper;
import io.vertx.rxjava3.core.file.FileSystem;

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

    private final String awsRole;

    private final List<FunctionReservation> functionReservations;

    private final long reservationId;

    private final Set<Long> faasFunctionIds = new HashSet<>();

    private final Set<Long> vmResourceIds = new HashSet<>();

    private final Set<Long> vmFunctionIds = new HashSet<>();

    private final String dockerUserName;

    private final  VPC vpc;

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
        this.awsRole = awsRole;
        this.functionReservations = functionReservations;
        this.reservationId = reservationId;
        this.dockerUserName = dockerUserName;
        this.vpc = vpc;
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
        StringBuilder functionNames = new StringBuilder(), functionPaths = new StringBuilder(),
            functionRuntimes = new StringBuilder(), functionTimeouts = new StringBuilder(),
            functionMemorySizes = new StringBuilder(), functionHandlers = new StringBuilder(),
            functionLayers = new StringBuilder();
        for (FunctionReservation functionReservation: functionReservations) {
            Resource resource = functionReservation.getResource();
            if (!resource.getPlatform().getPlatform().equals(PlatformEnum.LAMBDA.getValue())) {
                continue;
            }
            Function function = functionReservation.getFunction();
            String runtime = function.getRuntime().getName();
            String functionIdentifier =  function.getFunctionDeploymentId();
            functionNames.append("\"").append("r").append(resource.getResourceId())
                .append("_").append(functionIdentifier).append("_").append(reservationId)
                .append("\",");
            functionPaths.append("\"")
                .append(functionsDir.toAbsolutePath().toString().replace("\\","/")).append("/")
                .append(functionIdentifier)
                .append(".zip\",");
            if (runtime.startsWith("python")) {
                functionHandlers.append("\"main.handler\",");
                faasFunctionIds.add(function.getFunctionId());
            } else {
                throw new RuntimeNotSupportedException();
            }
            Map<String, MetricValue> metricValues = MetricValueMapper.mapMetricValues(resource.getMetricValues());
            functionTimeouts.append(metricValues.get("timeout").getValueNumber()).append(",");
            functionMemorySizes.append(metricValues.get("memory-size").getValueNumber()).append(",");
            functionLayers.append("[],");
            functionRuntimes.append("\"").append(runtime).append("\",");
        }
        if (faasFunctionIds.isEmpty()) {
            return "";
        }

        return String.format(
            "module \"lambda\" {\n" +
                "  source = \"../../../terraform/aws/faas\"\n" +
                "  names = [%s]\n" +
                "  paths = [%s]\n" +
                "  handlers = [%s]\n" +
                "  timeouts = [%s]\n" +
                "  memory_sizes = [%s]\n" +
                "  layers = [%s]\n" +
                "  runtimes = [%s]\n" +
                "  aws_role = \"%s\"\n" +
                "}\n", functionNames, functionPaths, functionHandlers, functionTimeouts,
            functionMemorySizes, functionLayers, functionRuntimes, awsRole
        );
    }

    @Override
    protected String getVmModulesString() {
        StringBuilder resourceNamesString = new StringBuilder(), instanceTypesString = new StringBuilder(),
            functionsString = new StringBuilder(), vmString = new StringBuilder();

        for (FunctionReservation functionReservation: functionReservations) {
            Resource resource = functionReservation.getResource();
            Function function = functionReservation.getFunction();
            if (!resource.getPlatform().getPlatform().equals(PlatformEnum.EC2.getValue())) {
                continue;
            }
            String resourceName = "resource_" + resource.getResourceId();
            Map<String, MetricValue> metricValues = resource.getMetricValues()
                .stream()
                .collect(Collectors.toMap(metricValue -> metricValue.getMetric().getMetric(),
                    metricValue -> metricValue));
            // TODO: swap with check, if vm is already deployed
            if (checkMustDeployVM(resource)) {
                resourceNamesString.append("\"").append(resourceName).append("\",");
                instanceTypesString.append("\"").append(metricValues.get("instance-type").getValueString())
                    .append("\",");
                vmResourceIds.add(resource.getResourceId());
            }
            String functionIdentifier =  function.getFunctionDeploymentId();
            functionsString.append(String.format(
                "module \"r%s_%s\" {\n" +
                    "  openfaas_depends_on = module.vm\n" +
                    "  source = \"../../../terraform/openfaas\"\n" +
                    "  name = \"r%s_%s_%s\"\n" +
                    "  image = \"%s/%s\"\n" +
                    "  basic_auth_user = \"admin\"\n" +
                    "  vm_props = module.vm.vm_props[\"%s\"]\n" +
                    "}\n", resource.getResourceId(), functionIdentifier, resource.getResourceId(),
                functionIdentifier, reservationId, dockerUserName,
                functionIdentifier, resourceName
            ));
            vmFunctionIds.add(function.getFunctionId());
        }

        if (vmResourceIds.isEmpty()) {
            return "";
        }
        vmString.append(String.format(
                "module \"vm\" {\n" +
                "  source         = \"../../../terraform/aws/vm\"\n" +
                "  reservation    = \"%s\"\n" +
                "  names          = [%s]\n" +
                "  instance_types = [%s]\n" +
                "  vpc_id         = \"%s\"\n" +
                "  subnet_id      = \"%s\"\n" +
                "}\n", reservationId, resourceNamesString, instanceTypesString,
            vpc.getVpcIdValue(), vpc.getSubnetIdValue()));
        vmString.append(functionsString);
        return vmString.toString();
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
        if (!this.vmResourceIds.isEmpty()) {
            String vmProps =
                "output \"vm_props\" {\n" +
                "  value = module.vm.vm_props\n" +
                "  sensitive = true\n" +
                "}\n";
            outputString.append(vmProps);
        }
        if (!this.vmFunctionIds.isEmpty()) {
            StringBuilder vmUrls = new StringBuilder(), vmFunctionIds = new StringBuilder();
            for (FunctionReservation functionReservation: functionReservations) {
                Resource resource = functionReservation.getResource();
                Function function = functionReservation.getFunction();
                String functionIdentifier = function.getFunctionDeploymentId();
                if (resource.getPlatform().getResourceType().getResourceType().equals("vm")) {
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
            this.getFunctionsModulString() +
            this.getVmModulesString();
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
