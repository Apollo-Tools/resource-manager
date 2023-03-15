package at.uibk.dps.rm.service.deployment;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;

public class DeploymentServiceImpl  implements DeploymentService {

    @Override
    public Future<Integer> deploy(JsonObject data) {
        DeploymentExecutor deploymentExecutor = new DeploymentExecutor(Vertx.currentContext().owner(), data);
        return Future.fromCompletionStage(deploymentExecutor.deploy()
            .doOnError(Throwable::printStackTrace)
            .toCompletionStage());
    }

    @Override
    public Future<Void> terminate(long resourceId) {
        return null;
    }
}
