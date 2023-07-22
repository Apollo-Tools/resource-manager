package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDTO;
import at.uibk.dps.rm.service.ServiceProxy;
import at.uibk.dps.rm.service.deployment.terraform.FunctionPrepareService;
import at.uibk.dps.rm.service.deployment.terraform.MainFileService;
import at.uibk.dps.rm.service.deployment.terraform.TerraformSetupService;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.rxjava3.SingleHelper;
import io.vertx.rxjava3.core.Vertx;

/**
 * This is the implementation of the #DeploymentExecutionService.
 *
 * @author matthi-g
 */
public class DeploymentExecutionServiceImpl extends ServiceProxy implements DeploymentExecutionService {

    private final Vertx vertx = Vertx.currentContext().owner();

    @Override
    public String getServiceProxyAddress() {
        return "deployment-execution" + super.getServiceProxyAddress();
    }

    @Override
    public Future<FunctionsToDeploy> packageFunctionsCode(DeployResourcesDTO deployRequest) {
        Single<FunctionsToDeploy> packageFunctions = new ConfigUtility(vertx).getConfig().flatMap(config -> {
            DeploymentPath deploymentPath = new DeploymentPath(deployRequest.getDeployment().getDeploymentId(),
                config);
            FunctionPrepareService functionFileService = new FunctionPrepareService(vertx,
                deployRequest.getFunctionDeployments(), deploymentPath,
                deployRequest.getDeploymentCredentials().getDockerCredentials());
            return functionFileService.packageCode();
        });
        return SingleHelper.toFuture(packageFunctions);
    }

    @Override
    public Future<DeploymentCredentials> setUpTFModules(DeployResourcesDTO deployRequest) {
        DeploymentCredentials credentials = new DeploymentCredentials();
        Single<DeploymentCredentials> createTFDirs = new ConfigUtility(vertx).getConfig().flatMap(config -> {
            DeploymentPath deploymentPath = new DeploymentPath(deployRequest.getDeployment().getDeploymentId(),
                config);
            TerraformSetupService tfSetupService = new TerraformSetupService(vertx, deployRequest,
                deploymentPath, credentials);
            return tfSetupService
            .setUpTFModuleDirs()
            .flatMapCompletable(tfModules -> {
                // TF: main files
                MainFileService mainFileService = new MainFileService(vertx.fileSystem(), deploymentPath.getRootFolder()
                    , tfModules);
                return mainFileService.setUpDirectory();
            })
            .andThen(Single.fromCallable(() -> credentials));
        });
        return SingleHelper.toFuture(createTFDirs);
    }

    @Override
    public Future<DeploymentCredentials> getNecessaryCredentials(TerminateResourcesDTO terminateRequest) {
        DeploymentCredentials credentials = new DeploymentCredentials();
        long deploymentId = terminateRequest.getDeployment().getDeploymentId();
        Single<DeploymentCredentials> getNecessaryCredentials = new ConfigUtility(vertx).getConfig().flatMap(config -> {
            DeploymentPath deploymentPath = new DeploymentPath(deploymentId, config);
            TerraformSetupService tfSetupService = new TerraformSetupService(vertx, terminateRequest, deploymentPath,
                credentials);
            return tfSetupService.getTerminationCredentials();
        });
        return SingleHelper.toFuture(getNecessaryCredentials);
    }
}
