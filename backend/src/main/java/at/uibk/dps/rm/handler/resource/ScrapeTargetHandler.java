package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.deployment.FunctionDeploymentService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

public class ScrapeTargetHandler extends ValidationHandler {

    private final FunctionDeploymentService functionDeploymentService;

    /**
     * Create an instance from the functionDeploymentService.
     *
     * @param functionDeploymentService the service
     */
    public ScrapeTargetHandler(FunctionDeploymentService functionDeploymentService) {
        super(functionDeploymentService);
        this.functionDeploymentService = functionDeploymentService;
    }

    public Single<JsonArray> getAllScrapeTargets() {
        return functionDeploymentService.findAllScrapeTargets();
    }
}
