package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.TerminateResourcesRequest;
import at.uibk.dps.rm.service.ServiceProxy;
import at.uibk.dps.rm.service.deployment.terraform.FunctionFileService;
import at.uibk.dps.rm.service.deployment.terraform.MainFileService;
import at.uibk.dps.rm.service.deployment.terraform.TerraformFileService;
import at.uibk.dps.rm.service.deployment.terraform.TerraformSetupService;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.util.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.rxjava3.CompletableHelper;
import io.vertx.rxjava3.SingleHelper;
import io.vertx.rxjava3.core.Vertx;

/**
 * This is the implementation of the #DeploymentService.
 *
 * @author matthi-g
 */
public class DeploymentServiceImpl extends ServiceProxy implements DeploymentService {

    private final Vertx vertx = Vertx.currentContext().owner();

    @Override
    public String getServiceProxyAddress() {
        return "deployment" + super.getServiceProxyAddress();
    }

    @Override
    public Future<FunctionsToDeploy> packageFunctionsCode(DeployResourcesRequest deployRequest) {
        Single<FunctionsToDeploy> packageFunctions = new ConfigUtility(vertx).getConfig().flatMap(config -> {
            DeploymentPath deploymentPath = new DeploymentPath(deployRequest.getReservation().getReservationId(),
                config);
            FunctionFileService functionFileService = new FunctionFileService(vertx,
                deployRequest.getFunctionResources(), deploymentPath.getFunctionsFolder(),
                deployRequest.getDockerCredentials());
            return functionFileService.packageCode();
        });
        return SingleHelper.toFuture(packageFunctions);
    }

    @Override
    public Future<DeploymentCredentials> setUpTFModules(DeployResourcesRequest deployRequest) {
        DeploymentCredentials credentials = new DeploymentCredentials();
        Single<DeploymentCredentials> createTFDirs = new ConfigUtility(vertx).getConfig().flatMap(config -> {
            DeploymentPath deploymentPath = new DeploymentPath(deployRequest.getReservation().getReservationId(),
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
    public Future<DeploymentCredentials> getNecessaryCredentials(TerminateResourcesRequest terminateRequest) {
        DeploymentCredentials credentials = new DeploymentCredentials();
        long reservationId = terminateRequest.getReservation().getReservationId();
        Single<DeploymentCredentials> getNecessaryCredentials = new ConfigUtility(vertx).getConfig().flatMap(config -> {
            DeploymentPath deploymentPath = new DeploymentPath(reservationId, config);
            TerraformSetupService tfSetupService = new TerraformSetupService(vertx, terminateRequest, deploymentPath,
                credentials);
            return tfSetupService.getTerminationCredentials();
        });
        return SingleHelper.toFuture(getNecessaryCredentials);
    }

    @Override
    public Future<Void> deleteTFDirs(long reservationId) {
        Completable deleteTFDirs = new ConfigUtility(vertx).getConfig().flatMapCompletable(config -> {
            DeploymentPath deploymentPath = new DeploymentPath(reservationId, config);
            return TerraformFileService.deleteAllDirs(vertx.fileSystem(), deploymentPath.getRootFolder());
        });
        return CompletableHelper.toFuture(deleteTFDirs);
    }
}
