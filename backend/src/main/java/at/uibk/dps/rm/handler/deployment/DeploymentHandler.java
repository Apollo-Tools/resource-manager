package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.deployment.DeploymentResponse;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Processes the http requests that concern the deployment entity.
 *
 * @author matthi-g
 */
public class DeploymentHandler extends ValidationHandler {

    private final DeploymentChecker deploymentChecker;

    private final ResourceDeploymentChecker resourceDeploymentChecker;

    private final FunctionDeploymentChecker functionDeploymentChecker;

    private final ServiceDeploymentChecker serviceDeploymentChecker;

    /**
     * Create an instance from the deploymentChecker, resourceDeploymentChecker, statusChecker,
     * deploymentExecutionHandler, deploymentErrorHandler and preconditionHandler
     *
     * @param deploymentChecker the deployment checker
     * @param resourceDeploymentChecker the resource deployment checker
     */
    public DeploymentHandler(DeploymentChecker deploymentChecker, ResourceDeploymentChecker resourceDeploymentChecker,
            FunctionDeploymentChecker functionDeploymentChecker, ServiceDeploymentChecker serviceDeploymentChecker) {
        super(deploymentChecker);
        this.deploymentChecker = deploymentChecker;
        this.resourceDeploymentChecker = resourceDeploymentChecker;
        this.functionDeploymentChecker = functionDeploymentChecker;
        this.serviceDeploymentChecker = serviceDeploymentChecker;
    }

    @Override
    protected Single<JsonObject> getOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> deploymentChecker.checkFindOne(id, rc.user().principal().getLong("account_id")))
            .flatMap(result -> functionDeploymentChecker
                    .checkFindAllByDeploymentId(result.getLong("deployment_id"))
                    .map(functionDeployments -> {
                        result.put("function_resources", functionDeployments);
                        return result;
                    })
                    .flatMap(deployment -> serviceDeploymentChecker
                        .checkFindAllByDeploymentId(deployment.getLong("deployment_id"))
                    .map(serviceDeployments -> {
                        deployment.put("service_resources", serviceDeployments);
                        return deployment;
                    })));
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return deploymentChecker.checkFindAll(accountId)
            .flatMap(result -> {
                List<Single<JsonObject>> singles = new ArrayList<>();
                for (Object object : result.getList()) {
                    JsonObject deployment = (JsonObject) object;
                    ((JsonObject) object).remove("is_active");
                    ((JsonObject) object).remove("created_by");
                    DeploymentResponse deploymentResponse = deployment.mapTo(DeploymentResponse.class);
                    singles.add(resourceDeploymentChecker.checkFindAllByDeploymentId(deploymentResponse.getDeploymentId())
                        .map(resourceDeploymentChecker::checkCrucialResourceDeploymentStatus)
                        .map(status -> {
                            deploymentResponse.setStatusValue(status);
                            return JsonObject.mapFrom(deploymentResponse);
                        }));
                }
                if (singles.isEmpty()) {
                    return Single.just(new ArrayList<>());
                }

                return Single.zip(singles, objects -> Arrays.stream(objects).map(object -> (JsonObject) object)
                    .collect(Collectors.toList()));
            })
            .map(JsonArray::new);
    }

    @Override
    public Single<JsonObject> postOneToAccount(RoutingContext rc) {
        return super.postOneToAccount(rc);
    }

    public Single<JsonObject> cancelDeployment(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> deploymentChecker.submitCancelDeployment(id, accountId));
    }
}
