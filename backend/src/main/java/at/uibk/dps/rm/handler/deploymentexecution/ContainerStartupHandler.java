package at.uibk.dps.rm.handler.deploymentexecution;

import at.uibk.dps.rm.entity.dto.function.InvocationResponseDTO;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.service.rxjava3.database.deployment.FunctionDeploymentService;
import at.uibk.dps.rm.service.rxjava3.database.deployment.ServiceDeploymentService;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.client.WebClient;
import lombok.RequiredArgsConstructor;

/**
 * Processes requests that concern startup of containers.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class ContainerStartupHandler {

    private final DeploymentExecutionChecker deploymentChecker;

    private final ServiceDeploymentService serviceDeploymentService;

    private final FunctionDeploymentService functionDeploymentService;

    private final WebClient webClient;

    /**
     * Deploy a container.
     *
     * @param rc the routing context
     */
    public void deployContainer(RoutingContext rc) {
        processDeployTerminateRequest(rc, true);
    }

    /**
     * Terminate a container.
     *
     * @param rc the routing context
     */
    public void terminateContainer(RoutingContext rc) {
        processDeployTerminateRequest(rc, false);
    }

    /**
     * Process a deploy or terminate request for containers.
     *
     * @param rc the routing context
     * @param isStartup if the request is for startup or termination of a container
     */
    private void processDeployTerminateRequest(RoutingContext rc, boolean isStartup) {
        long accountId = rc.user().principal().getLong("account_id");
        HttpHelper.getLongPathParam(rc, "deploymentId")
            .flatMap(deploymentId -> HttpHelper.getLongPathParam(rc, "serviceDeploymentId")
                .flatMap(serviceDeployment -> serviceDeploymentService
                    .existsReadyForContainerStartupAndTermination(deploymentId, serviceDeployment, accountId)
                    .flatMap(exists -> {
                        if (!exists) {
                            return Single.error(new NotFoundException(ServiceDeployment.class));
                        }
                        if (isStartup) {
                            long startTime = System.nanoTime();
                            return deploymentChecker.startContainer(deploymentId, serviceDeployment)
                                .map(result -> {
                                    long endTime = System.nanoTime();
                                    double startupTime = (endTime - startTime) / 1_000_000_000.0;
                                    result.put("startup_time_seconds", startupTime);
                                    return result;
                                });
                        } else {
                            return deploymentChecker.stopContainer(deploymentId, serviceDeployment)
                                .toSingle(JsonObject::new);
                        }
                    }))
            )
            .subscribe(result -> {
                    if (isStartup) {
                        rc.response().setStatusCode(200).end(result.encodePrettily());
                    } else {
                        rc.response().setStatusCode(204).end();
                    }
                },
                throwable -> {
                    Throwable throwable1 = throwable;
                    if (throwable instanceof DeploymentTerminationFailedException) {
                        throwable1 = new BadInputException("Deployment failed. See deployment logs for details.");
                    }
                    ResultHandler.handleRequestError(rc, throwable1);
                }
            );
    }

    public void invokeFunction(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        Buffer requestBody = rc.body() == null || rc.body().buffer() == null ? Buffer.buffer() : rc.body().buffer();
        MultiMap headers = rc.request().headers()
            .remove("Authorization")
            .remove("Host")
            .remove("User-Agent")
            .add("apollo-request-type", "rm");
        HttpHelper.getLongPathParam(rc, "deploymentId")
            .flatMap(deploymentId -> HttpHelper.getLongPathParam(rc, "functionDeploymentId")
                .flatMap(functionDeploymentId -> functionDeploymentService
                    .findOneForInvocation(functionDeploymentId, accountId)
                    .map(functionDeployment -> functionDeployment.mapTo(FunctionDeployment.class))
                    .flatMap(functionDeployment -> webClient.postAbs(functionDeployment.getDirectTriggerUrl())
                        .putHeaders(headers)
                        .sendBuffer(requestBody))
                )).subscribe(response -> {
                    // TODO: handle error
                    InvocationResponseDTO responseBody = response.bodyAsJson(InvocationResponseDTO.class);
                    rc.response().setStatusCode(response.statusCode()).end(responseBody.getBody());
                },
                throwable -> ResultHandler.handleRequestError(rc, throwable)
            );
    }
}
