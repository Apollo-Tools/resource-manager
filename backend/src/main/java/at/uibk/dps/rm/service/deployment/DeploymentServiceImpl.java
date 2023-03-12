package at.uibk.dps.rm.service.deployment;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class DeploymentServiceImpl  implements DeploymentService {

    private final DeploymentExecutor deploymentExecutor = new DeploymentExecutor();

    @Override
    public Future<Integer> deploy(JsonObject data) {
        return Future.fromCompletionStage(deploymentExecutor.deploy(data).toCompletionStage());
    }

    @Override
    public Future<Void> terminate(long resourceId) {
        return null;
    }
}
