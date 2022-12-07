package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class DeploymentExecutor {

    public Single<Long> deploy(JsonObject jsonObject) {
        DeployResourcesRequest deployResourcesRequest = jsonObject.mapTo(DeployResourcesRequest.class);
        return Single.just(1L);
    }
}
