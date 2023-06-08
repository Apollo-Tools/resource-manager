package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.EC2DeploymentData;
import at.uibk.dps.rm.entity.deployment.LambdaDeploymentData;
import at.uibk.dps.rm.entity.deployment.OpenFaasDeploymentData;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.deployment.TerraformModule;
import at.uibk.dps.rm.exception.PlatformNotSupportedException;
import at.uibk.dps.rm.service.deployment.util.ComposeDeploymentDataUtility;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.util.*;

/**
 * Extension of the #ModuleFileService to set up an AWS module of a deployment.
 *
 * @author matthi-g
 */
public class RegionFaasFileService extends TerraformFileService {

    private final TerraformModule module;

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
    public RegionFaasFileService(FileSystem fileSystem, Path rootFolder, Path functionsDir, Region region, String awsRole,
                          List<FunctionReservation> functionReservations, long reservationId, TerraformModule module,
                          String dockerUserName, VPC vpc) {
        super(fileSystem, rootFolder);
        this.module = module;
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
        for (FunctionReservation functionReservation: functionReservations) {
            Resource resource = functionReservation.getResource();
            Function function = functionReservation.getFunction();
            PlatformEnum platform = PlatformEnum.fromString(resource.getPlatform().getPlatform());
            switch (platform) {
                case LAMBDA:
                    ComposeDeploymentDataUtility.composeLambdaDeploymentData(resource, function, reservationId,
                        functionsDir, lambdaDeploymentData);
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

    protected void setModuleResourceTypes() {
        this.module.setHasFaas(lambdaDeploymentData.getFunctionCount() + ec2DeploymentData.getFunctionCount() +
            openFaasDeploymentData.getFunctionCount() > 0);
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
            "output \"function_urls\" {\n" +
                "  value = merge(%s, %s)\n" +
                "}\n", lambdaUrls, openFaasUrls
        ));
        setModuleResourceTypes();
        return outputString.toString();
    }
}
