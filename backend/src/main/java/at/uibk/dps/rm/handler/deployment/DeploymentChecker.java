package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentService;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;

public class DeploymentChecker {

    private final DeploymentService deploymentService;

    public DeploymentChecker(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public Completable deployResources(DeployResourcesRequest request) {
        JsonObject requestJson = JsonObject.mapFrom(request);
        return deploymentService.deploy(requestJson).ignoreElement();
    }
}
