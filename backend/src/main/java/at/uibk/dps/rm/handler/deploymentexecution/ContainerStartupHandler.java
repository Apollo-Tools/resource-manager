package at.uibk.dps.rm.handler.deploymentexecution;

import at.uibk.dps.rm.entity.dto.deployment.DeployTerminateServicesDTO;
import at.uibk.dps.rm.entity.dto.deployment.ServiceDeploymentId;
import at.uibk.dps.rm.entity.dto.deployment.StartTerminateServiceDeploymentDTO;
import at.uibk.dps.rm.entity.dto.function.InvocationResponseBodyDTO;
import at.uibk.dps.rm.entity.dto.function.InvokeFunctionDTO;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.entity.monitoring.ServiceStartupTerminateTime;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.exception.ForbiddenException;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.misc.HttpHelper;
import at.uibk.dps.rm.util.misc.MultiMapUtility;
import at.uibk.dps.rm.verticle.ApiVerticle;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
        boolean isAdmin =rc.user().principal().getJsonArray("role").contains("admin");
        StartTerminateServiceDeploymentDTO request =
            rc.body().asJsonObject().mapTo(StartTerminateServiceDeploymentDTO.class);
        if (!isAdmin && request.getIgnoreRunningStateChange()) {
            return Completable.error(new ForbiddenException("this operation is not allowed with the specified " +
                "parameters"));
        }
        List<Long> ids = request.getServiceDeployments().stream()
            .map(ServiceDeploymentId::getResourceDeploymentId)
            .collect(Collectors.toList());
        return serviceProxyProvider.getDeploymentService().findOneByIdAndAccountId(request.getDeploymentId(), accountId)
            .map(deployment -> {
                deployment.remove("function_resources");
                deployment.remove("service_resources");
                return deployment.mapTo(Deployment.class);
            })
            .flatMapCompletable(deployment -> serviceProxyProvider.getServiceDeploymentService()
                .findAllForStartupAndShutdown(ids, accountId, request.getDeploymentId(),
                    request.getIgnoreRunningStateChange())
                .flatMapObservable(Observable::fromIterable)
                .map(serviceDeployment -> ((JsonObject) serviceDeployment).mapTo(ServiceDeployment.class))
                .toList()
                .flatMapCompletable(serviceDeployments -> {
                    if (serviceDeployments.isEmpty()) {
                        return rc.response().setStatusCode(204).end();
                    }
                    long startTime = System.nanoTime();
                    DeployTerminateServicesDTO deployTerminateServices = new DeployTerminateServicesDTO(deployment,
                        serviceDeployments);
                    Single<JsonObject> startupStopServices;
                    if (isStartup) {
                        startupStopServices = deploymentChecker.startupServices(deployTerminateServices);
                    } else {
                        startupStopServices = deploymentChecker.stopServices(deployTerminateServices)
                            .andThen(Single.defer(() -> Single.just(new JsonObject())));
                    }
                    return startupStopServices.flatMapCompletable(result -> {
                        long endTime = System.nanoTime();
                        double executionTime = (endTime - startTime) / 1_000_000_000.0;
                        UUID requestId = UUID.randomUUID();
                        ServiceStartupTerminateTime serviceStartupTerminateTime =
                            new ServiceStartupTerminateTime(requestId.toString(), executionTime,
                                serviceDeployments, isStartup);
                        serviceProxyProvider.getContainerStartTermPushService()
                            .composeAndPushMetric(JsonObject.mapFrom(serviceStartupTerminateTime))
                            .subscribe();
                        result.put(isStartup ? "startup_time_seconds" : "termination_time_seconds",
                            executionTime);
                        return rc.response().setStatusCode(200).end(result.encodePrettily());
                    });
            }))
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
                        processResponse = serviceProxyProvider.getFunctionInvocationPushService()
                            .composeAndPushMetric(invocationResponse.getMonitoringData().getExecutionTimeMs() / 1000.0,
                                functionDeployment.getDeployment().getDeploymentId(),
                                functionDeployment.getResourceDeploymentId(),
                                functionDeployment.getFunction().getFunctionId(),
                                functionDeployment.getResource().getResourceId(),
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
