package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.deployment.TerraformModule;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class AWSFileService extends ModuleFileService {

    private final Path functionsDir;

    private final Region region;

    private final String awsRole;
    private final List<FunctionResource> functionResources;

    private final long reservationId;

    private final Set<Long> faasFunctionIds = new HashSet<>();

    private final Set<Long> vmResourceIds = new HashSet<>();

    private final Set<Long> vmFunctionIds = new HashSet<>();

    private final String dockerUserName;

    private final  VPC vpc;

    public AWSFileService(FileSystem fs, Path rootFolder, Path functionsDir, Region region, String awsRole,
                          List<FunctionResource> functionResources, long reservationId, TerraformModule module,
                          String dockerUserName, VPC vpc) {
        super(fs, rootFolder, module);
        this.functionsDir = functionsDir;
        this.region = region;
        this.awsRole = awsRole;
        this.functionResources = functionResources;
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

    // TODO: rework access to metric values
    @Override
    protected String getFunctionsModulString(List<FunctionResource> functionResources, long reservationId,
                                             Path rootFolder) {
        StringBuilder functionNames = new StringBuilder(), functionPaths = new StringBuilder(),
            functionRuntimes = new StringBuilder(), functionTimeouts = new StringBuilder(),
            functionMemorySizes = new StringBuilder(), functionHandlers = new StringBuilder(),
            functionLayers = new StringBuilder();
        for (FunctionResource fr: functionResources) {
            Resource resource = fr.getResource();
            if (!resource.getResourceType().getResourceType().equals("faas")) {
                continue;
            }
            Function function = fr.getFunction();
            String runtime = function.getRuntime().getName().toLowerCase();
            String functionIdentifier =  function.getName().toLowerCase() +
                "_" + runtime.replace(".", "");
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
            }
            Map<String, MetricValue> metricValues = resource.getMetricValues()
                .stream()
                .collect(Collectors.toMap(metricValue -> metricValue.getMetric().getMetric(),
                    metricValue -> metricValue));
            functionTimeouts.append(metricValues.get("timeout").getValueNumber()).append(",");
            functionMemorySizes.append(metricValues.get("memory-size").getValueNumber()).append(",");
            functionLayers.append("[],");
            functionRuntimes.append("\"").append(runtime).append("\",");
        }
        if (faasFunctionIds.isEmpty()) {
            return "";
        }

        return String.format(
            "module \"faas\" {\n" +
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

    private boolean checkMustDeployVM(Resource resource) {
        return resource.getResourceType().getResourceType().equals("vm") &&
            !resource.getIsSelfManaged() && !vmResourceIds.contains(resource.getResourceId());
    }

    @Override
    protected String getVmModulesString(List<FunctionResource> functionResources) {
        StringBuilder resourceNamesString = new StringBuilder(), instanceTypesString = new StringBuilder(),
            functionsString = new StringBuilder(), vmString = new StringBuilder();

        for (FunctionResource functionResource: functionResources) {
            Resource resource = functionResource.getResource();
            Function function = functionResource.getFunction();
            String resourceName = "resource_" + resource.getResourceId();
            Map<String, MetricValue> metricValues = resource.getMetricValues()
                .stream()
                .collect(Collectors.toMap(metricValue -> metricValue.getMetric().getMetric(),
                    metricValue -> metricValue));
            if (checkMustDeployVM(resource)) {
                resourceNamesString.append("\"").append(resourceName).append("\",");
                instanceTypesString.append("\"").append(metricValues.get("instance-type").getValueString())
                    .append("\",");
                vmResourceIds.add(resource.getResourceId());
            }
            if (resource.getResourceType().getResourceType().equals("vm") && !resource.getIsSelfManaged()) {
                String runtime = function.getRuntime().getName().toLowerCase();
                String functionIdentifier =  function.getName().toLowerCase() +
                    "_" + runtime.replace(".", "");
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

        // TODO: get vpc from persisted values
        return vmString.toString();
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

    // TODO: rework for vms
    @Override
    protected String getOutputString() {
        StringBuilder outputString = new StringBuilder();
        if (!this.faasFunctionIds.isEmpty()) {
            String functionUrl =
                "output \"function_urls\" {\n" +
                "  value = module.faas.function_urls\n" +
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
            for (FunctionResource functionResource: functionResources) {
                Resource resource = functionResource.getResource();
                Function function = functionResource.getFunction();
                String runtime = function.getRuntime().getName().toLowerCase();
                String functionIdentifier = function.getName().toLowerCase() +
                    "_" + runtime.replace(".", "");
                if (resource.getResourceType().getResourceType().equals("vm")) {
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
            this.getFunctionsModulString(functionResources, reservationId, getRootFolder()) +
            this.getVmModulesString(functionResources);
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
