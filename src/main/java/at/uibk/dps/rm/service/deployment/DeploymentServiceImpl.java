package at.uibk.dps.rm.service.deployment;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class DeploymentServiceImpl  implements DeploymentService {

    @Override
    public Future<JsonObject> deploy(JsonObject data) {
        return null;
    }

    @Override
    public Future<Void> terminate(long resourceId) {
        return null;
    }
}
