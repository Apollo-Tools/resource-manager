package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDTO;
import at.uibk.dps.rm.service.ServiceProxy;
import at.uibk.dps.rm.service.deployment.docker.DockerHubImageChecker;
import at.uibk.dps.rm.service.deployment.docker.DockerImageChecker;
import at.uibk.dps.rm.service.deployment.terraform.FunctionPrepareService;
import at.uibk.dps.rm.service.deployment.terraform.MainFileService;
import at.uibk.dps.rm.service.deployment.terraform.TerraformSetupService;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
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
    public void packageFunctionsCode(DeployResourcesDTO deployRequest,
            Handler<AsyncResult<FunctionsToDeploy>> resultHandler) {
        Single<FunctionsToDeploy> packageFunctions = new ConfigUtility(vertx).getConfigDTO().flatMap(config -> {
            DockerImageChecker dockerImageChecker = new DockerHubImageChecker(vertx, deployRequest
                .getDeploymentCredentials().getDockerCredentials());
            return dockerImageChecker.getNecessaryFunctionBuilds(deployRequest.getFunctionDeployments())
                .flatMap(openFaasFunctions -> {
                    DeploymentPath deploymentPath = new DeploymentPath(deployRequest.getDeployment().getDeploymentId(),
                        config);
                    FunctionPrepareService functionFileService = new FunctionPrepareService(vertx,
                        deployRequest.getFunctionDeployments(), deploymentPath, openFaasFunctions,
                        deployRequest.getDeploymentCredentials().getDockerCredentials());
                    return functionFileService.packageCode();
                });
        });
        RxVertxHandler.handleSession(packageFunctions, resultHandler);
    }

    @Override
    public void setUpTFModules(DeployResourcesDTO deployRequest,
            Handler<AsyncResult<DeploymentCredentials>> resultHandler) {
        DeploymentCredentials credentials = new DeploymentCredentials();
        Single<DeploymentCredentials> createTFDirs = new ConfigUtility(vertx).getConfigDTO().flatMap(config -> {
            DeploymentPath deploymentPath = new DeploymentPath(deployRequest.getDeployment().getDeploymentId(),
                config);
            TerraformSetupService tfSetupService = new TerraformSetupService(vertx, deployRequest,
                deploymentPath, credentials);
            return tfSetupService
            .setUpTFModuleDirs(config)
            .flatMapCompletable(tfModules -> {
                // TF: main files
                MainFileService mainFileService = new MainFileService(vertx.fileSystem(), deploymentPath.getRootFolder()
                    , tfModules);
                return mainFileService.setUpDirectory();
            })
            .andThen(Single.fromCallable(() -> credentials));
        });
        RxVertxHandler.handleSession(createTFDirs, resultHandler);
    }

    @Override
    public void getNecessaryCredentials(TerminateResourcesDTO terminateRequest,
            Handler<AsyncResult<DeploymentCredentials>> resultHandler) {
        DeploymentCredentials credentials = new DeploymentCredentials();
        long deploymentId = terminateRequest.getDeployment().getDeploymentId();
        Single<DeploymentCredentials> getNecessaryCredentials = new ConfigUtility(vertx).getConfigDTO()
            .flatMap(config -> {
                DeploymentPath deploymentPath = new DeploymentPath(deploymentId, config);
                TerraformSetupService tfSetupService = new TerraformSetupService(vertx, terminateRequest, deploymentPath,
                    credentials);
                return tfSetupService.getTerminationCredentials();
            });
        RxVertxHandler.handleSession(getNecessaryCredentials, resultHandler);
    }
}
