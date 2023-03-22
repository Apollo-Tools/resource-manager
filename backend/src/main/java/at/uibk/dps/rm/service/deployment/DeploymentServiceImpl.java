package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.deployment.TerraformModule;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.TerminateResourcesRequest;
import at.uibk.dps.rm.service.deployment.terraform.FunctionFileService;
import at.uibk.dps.rm.service.deployment.terraform.MainFileService;
import at.uibk.dps.rm.service.deployment.terraform.TerraformSetupService;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.rxjava3.SingleHelper;
import io.vertx.rxjava3.core.Vertx;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DeploymentServiceImpl  implements DeploymentService {

    private final Vertx vertx = Vertx.currentContext().owner();

    @Override
    public Future<FunctionsToDeploy> packageFunctionsCode(DeployResourcesRequest deployRequest) {
        DeploymentPath deploymentPath = new DeploymentPath(deployRequest.getReservation().getReservationId());
        FunctionFileService functionFileService = new FunctionFileService(vertx,
            deployRequest.getFunctionResources(), deploymentPath.getFunctionsFolder(), deployRequest.getDockerCredentials());
        return SingleHelper.toFuture(functionFileService.packageCode());
    }

    @Override
    public Future<DeploymentCredentials> setUpTFModules(DeployResourcesRequest deployRequest) {
        DeploymentCredentials credentials = new DeploymentCredentials();
        DeploymentPath deploymentPath = new DeploymentPath(deployRequest.getReservation().getReservationId());
        TerraformSetupService tfSetupService = new TerraformSetupService(vertx, deployRequest,
            deploymentPath, credentials);
        List<Single<TerraformModule>> singles = tfSetupService.setUpTFModuleDirs();
        Single<DeploymentCredentials> createTFDirs = Single.zip(singles, objects -> Arrays.stream(objects).map(object -> (TerraformModule) object)
            .collect(Collectors.toList()))
            .flatMapCompletable(tfModules -> {
                // TF: main files
                MainFileService mainFileService = new MainFileService(vertx.fileSystem(), deploymentPath.getRootFolder()
                    , tfModules);
                return mainFileService.setUpDirectory();
            })
            .andThen(Single.fromCallable(() -> credentials));
        return SingleHelper.toFuture(createTFDirs);
    }

    @Override
    public Future<DeploymentCredentials> getNecessaryCredentials(TerminateResourcesRequest terminateRequest) {
        Promise<DeploymentCredentials> promise = Promise.promise();
        DeploymentCredentials credentials = new DeploymentCredentials();
        DeploymentPath deploymentPath = new DeploymentPath(terminateRequest.getReservation().getReservationId());
        TerraformSetupService tfSetupService = new TerraformSetupService(vertx, terminateRequest, deploymentPath,
            credentials);
        credentials = tfSetupService.getDeploymentCredentials();
        promise.complete(credentials);
        return promise.future();
    }

    @Override
    public Future<Void> terminate(long resourceId) {
        return null;
    }
}
