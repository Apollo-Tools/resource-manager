package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.EC2DeploymentData;
import at.uibk.dps.rm.entity.deployment.LambdaDeploymentData;
import at.uibk.dps.rm.entity.deployment.OpenFaasDeploymentData;
import at.uibk.dps.rm.entity.deployment.module.FaasModule;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.PlatformNotSupportedException;
import at.uibk.dps.rm.service.deployment.util.ComposeDeploymentDataUtility;
import io.vertx.rxjava3.core.file.FileSystem;

import java.util.*;

/**
 * Extension of the #ModuleFileService to set up an AWS module of a deployment.
 *
 * @author matthi-g
 */
public class RegionFaasFileService extends TerraformFileService {

    private final FaasModule module;

    private final Region region;

    private final DeploymentPath deploymentPath;

    private final List<FunctionDeployment> functionDeployments;

    private final long deploymentId;

    private final LambdaDeploymentData lambdaDeploymentData;

    private final EC2DeploymentData ec2DeploymentData;

    private final OpenFaasDeploymentData openFaasDeploymentData;

    /**
     * Create an instance from the fileSystem, rootFolder, functionsDir, region, awsRole,
     * functionDeployments, deploymentId, module, dockerUserName and vpc.
     *
     * @param fileSystem the vertx file system
     * @param deploymentPath the deployment path of the module
     * @param region the region where the resources are deployed
     * @param functionDeployments the list of function deployments
     * @param deploymentId the id of the deployment
     * @param module the terraform module
     * @param dockerUserName the docker username
     * @param vpc the virtual private cloud to use for the deployment
     */
    public RegionFaasFileService(FileSystem fileSystem, DeploymentPath deploymentPath, Region region,
                          List<FunctionDeployment> functionDeployments, long deploymentId, FaasModule module,
                          String dockerUserName, VPC vpc) {
        super(fileSystem, deploymentPath.getModuleFolder(module));
        this.module = module;
        this.deploymentPath = deploymentPath;
        this.region = region;
        this.functionDeployments = functionDeployments;
        this.deploymentId = deploymentId;
        this.lambdaDeploymentData = new LambdaDeploymentData(deploymentId, deploymentPath.getLayersFolder());
        this.ec2DeploymentData = new EC2DeploymentData(deploymentId, vpc, dockerUserName);
        this.openFaasDeploymentData = new OpenFaasDeploymentData(deploymentId, dockerUserName);
    }


    @Override
    protected String getProviderString() {
        String providerString = "";
        if (region.getResourceProvider().getProvider().equals(ResourceProviderEnum.AWS.getValue())) {
            providerString = String.format(
                "provider \"aws\" {\n" +
                    "  access_key = var.access_key\n" +
                    "  secret_key = var.secret_access_key\n" +
                    "  token = var.session_token\n" +
                    "  region = \"%s\"\n" +
                    "}\n", region.getName());
        }
        return providerString;
    }

    protected String getFunctionsModuleString() {
        for (FunctionDeployment functionDeployment: functionDeployments) {
            Resource resource = functionDeployment.getResource();
            Function function = functionDeployment.getFunction();
            PlatformEnum platform = PlatformEnum.fromPlatform(resource.getPlatform());
            switch (platform) {
                case LAMBDA:
                    ComposeDeploymentDataUtility.composeLambdaDeploymentData(resource, function, deploymentId,
                        deploymentPath.getFunctionsFolder(), lambdaDeploymentData);
                    break;
                case EC2:
                    ComposeDeploymentDataUtility.composeEC2DeploymentData(resource, function, ec2DeploymentData);
                    break;
                case OPENFAAS:
                    ComposeDeploymentDataUtility.composeOpenFassDeploymentData(resource, function, openFaasDeploymentData);
                    break;
                default:
                    throw new PlatformNotSupportedException("platform " + platform.getValue() + " not supported on " +
                        this.module.getResourceProvider().getValue());
            }
        }

        return lambdaDeploymentData.getModuleString() + ec2DeploymentData.getModuleString() +
            openFaasDeploymentData.getModuleString();
    }

    @Override
    protected String getMainFileContent() {
        return this.getProviderString() + this.getFunctionsModuleString();
    }

    @Override
    protected String getVariablesFileContent() {
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
    protected String getOutputsFileContent() {
        StringBuilder outputString = new StringBuilder();
        String lambdaUrls = "{}", openFaasUrls = "{}";
        if (this.lambdaDeploymentData.getFunctionCount() > 0) {
            lambdaUrls = "module.lambda.function_urls";
        }
        if (this.ec2DeploymentData.getFunctionCount() > 0 || this.openFaasDeploymentData.getFunctionCount() > 0) {
            StringBuilder vmUrls = new StringBuilder(), vmFunctionIds = new StringBuilder();
            for (FunctionDeployment functionDeployment: functionDeployments) {
                Resource resource = functionDeployment.getResource();
                Function function = functionDeployment.getFunction();
                String functionIdentifier = function.getFunctionDeploymentId();
                PlatformEnum platformEnum = PlatformEnum.fromPlatform(resource.getPlatform());
                if (platformEnum.equals(PlatformEnum.EC2) || platformEnum.equals(PlatformEnum.OPENFAAS)) {
                    vmUrls.append(String.format("module.r%s_%s.function_url,",
                        resource.getResourceId(), functionIdentifier));
                    vmFunctionIds.append(String.format("\"r%s_%s_%s\",",
                        resource.getResourceId(), functionIdentifier, deploymentId));
                }
            }
            openFaasUrls = String.format("zipmap([%s], [%s])", vmFunctionIds, vmUrls);
        }
        outputString.append(String.format(
            "output \"function_urls\" {\n" +
                "  value = merge(%s, %s)\n" +
                "}\n", lambdaUrls, openFaasUrls
        ));
        return outputString.toString();
    }
}
