package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.service.deployment.terraform.FunctionFileService;
import at.uibk.dps.rm.util.DeploymentPath;
import io.vertx.core.Future;
import io.vertx.rxjava3.SingleHelper;
import io.vertx.rxjava3.core.Vertx;

public class DeploymentServiceImpl  implements DeploymentService {

    @Override
    public Future<FunctionsToDeploy> packageFunctionsCode(DeployResourcesRequest deployRequest) {
        DeploymentPath deploymentPath = new DeploymentPath(deployRequest.getReservationId());
        FunctionFileService functionFileService = new FunctionFileService(Vertx.currentContext().owner(),
            deployRequest.getFunctionResources(), deploymentPath.getFunctionsFolder(), deployRequest.getDockerCredentials());
        return SingleHelper.toFuture(functionFileService.packageCode());
    }

    @Override
    public Future<Integer> deploy(DeployResourcesRequest deployRequest) {
        DeploymentExecutor deploymentExecutor = new DeploymentExecutor(Vertx.currentContext().owner(), deployRequest);
        return Future.fromCompletionStage(deploymentExecutor.deploy()
            .doOnError(Throwable::printStackTrace)
            .toCompletionStage());
    }

    @Override
    public Future<Void> terminate(long resourceId) {
        return null;
    }
}
