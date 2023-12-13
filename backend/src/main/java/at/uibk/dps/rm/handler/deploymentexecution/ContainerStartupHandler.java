package at.uibk.dps.rm.handler.deploymentexecution;

import at.uibk.dps.rm.entity.dto.function.InvocationResponseBodyDTO;
import at.uibk.dps.rm.entity.dto.function.InvokeFunctionDTO;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.misc.HttpHelper;
import at.uibk.dps.rm.util.misc.MultiMapUtility;
import at.uibk.dps.rm.verticle.ApiVerticle;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Processes requests that concern startup of containers.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class ContainerStartupHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiVerticle.class);

    private final DeploymentExecutionChecker deploymentChecker;

    private final ServiceProxyProvider serviceProxyProvider;

    /**
     * Deploy a container.
     *
     * @param rc the routing context
     * @return a Completable
     */
    public Completable deployContainer(RoutingContext rc) {
        return processDeployTerminateRequest(rc, true);
    }

    /**
     * Terminate a container.
     *
     * @param rc the routing context
     * @return a Completable
     */
    public Completable terminateContainer(RoutingContext rc) {
        return processDeployTerminateRequest(rc, false);
    }

    /**
     * Process a deploy or terminate request for containers.
     *
     * @param rc the routing context
     * @param isStartup if the request is for startup or termination of a container
     * @return a Completable
     */
    private Completable processDeployTerminateRequest(RoutingContext rc, boolean isStartup) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(serviceDeploymentId -> serviceProxyProvider.getServiceDeploymentService()
                .findOneForDeploymentAndTermination(serviceDeploymentId, accountId))
            .flatMapCompletable(existingServiceDeployment -> {
                ServiceDeployment serviceDeployment = existingServiceDeployment.mapTo(ServiceDeployment.class);
                if (isStartup) {
                    long startTime = System.nanoTime();
                    return deploymentChecker.startContainer(serviceDeployment)
                        .flatMapCompletable(result -> {
                            long endTime = System.nanoTime();
                            double startupTime = (endTime - startTime) / 1_000_000_000.0;
                            result.put("startup_time_seconds", startupTime);
                            return rc.response().setStatusCode(200).end(result.encodePrettily());
                        });
                } else {
                    return deploymentChecker.stopContainer(serviceDeployment)
                        .andThen(Single.defer(() -> Single.just(1L))
                        .flatMapCompletable(res -> rc.response().setStatusCode(204).end()));
                }
            })
            .onErrorResumeNext(throwable -> {
                if (throwable instanceof DeploymentTerminationFailedException) {
                    return Completable.error(new BadInputException("Deployment failed. See deployment logs for " +
                        "details."));
                } else {
                    return Completable.error(throwable);
                }
            });
    }

    /**
     * Invoke a function using the resource manager as a proxy.
     *
     * @param rc the routing context
     * @return a Completable
     */
    public Completable invokeFunction(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        Buffer requestBody = rc.body() == null || rc.body().buffer() == null ? Buffer.buffer() : rc.body().buffer();
        MultiMap headers = rc.request().headers()
            .remove("Authorization")
            .remove("Host")
            .remove("User-Agent")
            .add("apollo-request-type", "rm");
        Map<String, JsonArray> serializedHeaders = MultiMapUtility.serializeMultimap(headers);
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(functionDeploymentId -> serviceProxyProvider.getFunctionDeploymentService()
                .findOneForInvocation(functionDeploymentId, accountId))
            .map(functionDeployment -> functionDeployment.mapTo(FunctionDeployment.class))
            .flatMapCompletable(functionDeployment -> serviceProxyProvider.getFunctionExecutionService()
                .invokeFunction(functionDeployment.getDirectTriggerUrl(), requestBody.toString(), serializedHeaders)
                .flatMapCompletable(result -> {
                    InvokeFunctionDTO invokeFunctionDTO = result.mapTo(InvokeFunctionDTO.class);
                    String body = invokeFunctionDTO.getBody();
                    Completable processResponse = Completable.complete();
                    try {
                        InvocationResponseBodyDTO invocationResponse = new JsonObject(body)
                            .mapTo(InvocationResponseBodyDTO.class);
                        body = invocationResponse.getBody();
                        processResponse = serviceProxyProvider.getFunctionDeploymentService()
                            .saveExecTime(functionDeployment.getResourceDeploymentId(),
                                (int) invocationResponse.getMonitoringData().getExecutionTimeMs(),
                                requestBody.toString());
                    } catch (DecodeException ex) {
                        logger.info("failed to decode response: " + body.substring(0, Math.min(50, body.length())));
                    }
                    String finalBody = body;
                    return processResponse
                        .andThen(Single.defer(() -> Single.just(1L)))
                        .flatMapCompletable(res -> rc.response().setStatusCode(invokeFunctionDTO.getStatusCode())
                            .end(finalBody));
                }));
    }
}
