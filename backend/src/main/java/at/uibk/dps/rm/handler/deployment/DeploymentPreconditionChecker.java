package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.service.rxjava3.database.deployment.DeploymentPreconditionService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

import java.util.List;

public class DeploymentPreconditionChecker {

    private final DeploymentPreconditionService service;

    /**
     * Create an instance from a service interface.
     *
     * @param service the service to use
     */
    public DeploymentPreconditionChecker(DeploymentPreconditionService service) {
        this.service = service;
    }

    public Single<JsonArray> checkDeploymentIsValid(DeployResourcesRequest requestDTO, long accountId,
        List<VPC> vpcList) {
        return service.checkDeploymentIsValid(accountId, requestDTO);
    }
}
