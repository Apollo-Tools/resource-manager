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

    /**
     * Create an instance from the deploymentChecker, resourceDeploymentChecker
     *
     * @param deploymentChecker the deployment checker
     * @param resourceDeploymentChecker the resource deployment checker
     */
    public DeploymentHandler(DeploymentChecker deploymentChecker, ResourceDeploymentChecker resourceDeploymentChecker) {
        super(deploymentChecker);
        this.deploymentChecker = deploymentChecker;
        this.resourceDeploymentChecker = resourceDeploymentChecker;
    }

    @Override
    protected Single<JsonArray> getAllFromAccount(RoutingContext rc) {
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
