package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DeploymentCredentials;
import at.uibk.dps.rm.entity.dto.deployment.DeploymentResponse;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionChecker;
import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(DeploymentExecutionChecker.class);

    private final DeploymentChecker deploymentChecker;

    private final ResourceDeploymentChecker resourceDeploymentChecker;

    private final FunctionDeploymentChecker functionDeploymentChecker;

    private final ServiceDeploymentChecker serviceDeploymentChecker;


    //TODO: move this to the router
    private final DeploymentExecutionHandler deploymentExecutionHandler;


    //TODO: move this to the router
    private final DeploymentErrorHandler deploymentErrorHandler;

    /**
     * Create an instance from the deploymentChecker, resourceDeploymentChecker, statusChecker,
     * deploymentExecutionHandler, deploymentErrorHandler and preconditionHandler
     *
     * @param deploymentChecker the deployment checker
     * @param resourceDeploymentChecker the resource deployment checker
     * @param deploymentExecutionHandler the deployment execution handler
     * @param deploymentErrorHandler the deployment error handler
     */
    public DeploymentHandler(DeploymentChecker deploymentChecker, ResourceDeploymentChecker resourceDeploymentChecker,
            FunctionDeploymentChecker functionDeploymentChecker, ServiceDeploymentChecker serviceDeploymentChecker,
            DeploymentExecutionHandler deploymentExecutionHandler, DeploymentErrorHandler deploymentErrorHandler) {
        super(deploymentChecker);
        this.deploymentChecker = deploymentChecker;
        this.resourceDeploymentChecker = resourceDeploymentChecker;
        this.functionDeploymentChecker = functionDeploymentChecker;
        this.serviceDeploymentChecker = serviceDeploymentChecker;
        this.deploymentExecutionHandler = deploymentExecutionHandler;
        this.deploymentErrorHandler = deploymentErrorHandler;
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
    public Single<JsonObject> postOne(RoutingContext rc) {
        DeployResourcesRequest requestDTO = rc.body()
                .asJsonObject()
                .mapTo(DeployResourcesRequest.class);
        long accountId = rc.user().principal().getLong("account_id");
        return deploymentChecker.submitCreate(accountId, rc.body().asJsonObject())
            .map(deploymentJson -> {
                Deployment deployment = deploymentJson.mapTo(Deployment.class);
                initiateDeployment(deployment, accountId, requestDTO.getCredentials());
                return deploymentJson;
            });
    }

    @Override
    protected Completable updateOne(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> deploymentChecker.submitCancelDeployment(id, accountId))
            .flatMapCompletable(deploymentJson -> {
                Deployment deployment = deploymentJson.mapTo(Deployment.class);
                initiateTermination(deployment, accountId);
                return Completable.complete();
            });
    }

    /**
     * Execute the deployment of the resources contained in the deployment.
     *
     * @param deployment the deployment
     * @param accountId the id of the creator of the deployment
     * @param credentials the deployment credentials
     */
    // TODO: add check for kubeconfig
    private void initiateDeployment(Deployment deployment, long accountId, DeploymentCredentials credentials) {
        deploymentExecutionHandler.deployResources(deployment, accountId, credentials)
            .andThen(Completable.defer(() ->
                resourceDeploymentChecker.submitUpdateStatus(deployment.getDeploymentId(),
                    DeploymentStatusValue.DEPLOYED)))
            .doOnError(throwable -> logger.error(throwable.getMessage()))
            .onErrorResumeNext(throwable -> deploymentErrorHandler.onDeploymentError(accountId,
                deployment, throwable))
            .subscribe();
    }

    /**
     * Execute the termination of the resources contained in the deployment.
     *
     * @param deployment the deployment
     * @param accountId the id of the creator of the deployment
     */
    private void initiateTermination(Deployment deployment, long accountId) {
        deploymentChecker.checkFindOne(deployment.getDeploymentId(), accountId)
            .flatMapCompletable(res -> deploymentExecutionHandler.terminateResources(deployment, accountId))
            .andThen(Completable.defer(() ->
                resourceDeploymentChecker.submitUpdateStatus(deployment.getDeploymentId(),
                    DeploymentStatusValue.TERMINATED)))
            .doOnError(throwable -> logger.error(throwable.getMessage()))
            .onErrorResumeNext(throwable -> deploymentErrorHandler.onTerminationError(deployment, throwable))
            .subscribe();
    }
}
