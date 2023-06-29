package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.KubeConfig;
import at.uibk.dps.rm.entity.dto.credentials.k8s.Cluster;
import at.uibk.dps.rm.entity.dto.credentials.k8s.Context;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.DeploymentResponse;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionChecker;
import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import at.uibk.dps.rm.util.misc.MetricValueMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
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

    private final ResourceDeploymentStatusChecker statusChecker;

    private final FunctionDeploymentChecker functionDeploymentChecker;

    private final ServiceDeploymentChecker serviceDeploymentChecker;


    //TODO: move this to the router
    private final DeploymentExecutionHandler deploymentHandler;


    //TODO: move this to the router
    private final DeploymentErrorHandler deploymentErrorHandler;


    //TODO: move this to the router
    private final DeploymentPreconditionHandler preconditionHandler;

    /**
     * Create an instance from the deploymentChecker, resourceDeploymentChecker, statusChecker,
     * deploymentHandler, deploymentErrorHandler and preconditionHandler
     *
     * @param deploymentChecker the deployment checker
     * @param resourceDeploymentChecker the resource deployment checker
     * @param statusChecker the status checker
     * @param deploymentHandler the deployment handler
     * @param deploymentErrorHandler the deployment error handler
     * @param preconditionHandler the precondition handler
     */
    public DeploymentHandler(DeploymentChecker deploymentChecker, ResourceDeploymentChecker resourceDeploymentChecker,
          FunctionDeploymentChecker functionDeploymentChecker, ServiceDeploymentChecker serviceDeploymentChecker,
          ResourceDeploymentStatusChecker statusChecker, DeploymentExecutionHandler deploymentHandler,
          DeploymentErrorHandler deploymentErrorHandler, DeploymentPreconditionHandler preconditionHandler) {
        super(deploymentChecker);
        this.deploymentChecker = deploymentChecker;
        this.resourceDeploymentChecker = resourceDeploymentChecker;
        this.functionDeploymentChecker = functionDeploymentChecker;
        this.serviceDeploymentChecker = serviceDeploymentChecker;
        this.statusChecker = statusChecker;
        this.deploymentHandler = deploymentHandler;
        this.deploymentErrorHandler = deploymentErrorHandler;
        this.preconditionHandler = preconditionHandler;
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
        List<VPC> vpcList = new ArrayList<>();
        return preconditionHandler.checkDeploymentIsValid(requestDTO, accountId, vpcList)
            .flatMap(resources -> deploymentChecker.submitCreateDeployment(accountId)
                .flatMap(deploymentJson ->
                    statusChecker.checkFindOneByStatusValue(DeploymentStatusValue.NEW.name())
                        .map(statusNew -> statusNew.mapTo(ResourceDeploymentStatus.class))
                        .flatMap(statusNew -> createResourceDeploymentMap(deploymentJson, requestDTO, statusNew,
                            resources))
                    //TODO: remove self managed state (use edge instead of self managed vm) */
                    .flatMap(resourceDeployments -> functionDeploymentChecker
                        .submitCreateAll(Json.encodeToBuffer(resourceDeployments.get("function")).toJsonArray())
                        .andThen(serviceDeploymentChecker.submitCreateAll(Json
                            .encodeToBuffer(resourceDeployments.get("service")).toJsonArray()))
                        .andThen(Single.defer(() -> Single.just(1)))
                        .map(res -> {
                            Deployment deployment = deploymentJson.mapTo(Deployment.class);
                            initiateDeployment(deployment, accountId, requestDTO, vpcList);
                            return deploymentJson;
                        })
                    )
                )
            );
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

    private Single<Map<String, List<ResourceDeployment>>> createResourceDeploymentMap(JsonObject deploymentJson,
            DeployResourcesRequest request, ResourceDeploymentStatus status, JsonArray resources) {
        List<ResourceDeployment> functionDeployments = new ArrayList<>();
        List<ResourceDeployment> serviceDeployments = new ArrayList<>();
        Deployment deployment = deploymentJson.mapTo(Deployment.class);
        List<Resource> resouceList;
        KubeConfig kubeConfig = new KubeConfig();
        try {
            resouceList = DatabindCodec.mapper().readValue(resources.toString(), new TypeReference<>() {});
            if (!request.getServiceResources().isEmpty()) {
                kubeConfig = new YAMLMapper().readValue(request.getKubeConfig(), KubeConfig.class);
            }
        } catch (JsonProcessingException e) {
            return Single.error(new BadInputException("Unsupported schema of kube config"));
        }
        for (FunctionResourceIds functionResourceIds : request.getFunctionResources()) {
            Resource resource = new Resource();
            resource.setResourceId(functionResourceIds.getResourceId());
            Function function = new Function();
            function.setFunctionId(functionResourceIds.getFunctionId());
            functionDeployments.add(createNewResourceDeployment(deployment, resource, function, status));
        }
        for (ServiceResourceIds serviceResourceIds : request.getServiceResources()) {
            Resource resource = resouceList.stream()
                .filter(r -> r.getResourceId() == serviceResourceIds.getResourceId())
                .findFirst()
                .orElse(new Resource());
            resource.setResourceId(serviceResourceIds.getResourceId());
            Map<String, MetricValue> metricValues = MetricValueMapper.mapMetricValues(resource.getMetricValues());
            String clusterUrl = metricValues.get("cluster-url").getValueString();
            Context context = getContextByClusterUrl(kubeConfig, clusterUrl);
            String namespace = context.getContext().getNamespace() != null ? context.getContext().getNamespace() :
                "default";
            Service service = new Service();
            service.setServiceId(serviceResourceIds.getServiceId());
            serviceDeployments.add(createNewResourceDeployment(deployment, resource, service,
                namespace, context.getName(), status));
        }
        Map<String, List<ResourceDeployment>> resourceDeployments = Map.of("function", functionDeployments,
            "service", serviceDeployments);
        return Single.just(resourceDeployments);
    }

    private Context getContextByClusterUrl(KubeConfig kubeConfig, String clusterUrl) {
        Cluster cluster = kubeConfig.getClusters().stream()
            .filter(c -> c.getCluster().getServer().equals(clusterUrl))
            .findFirst()
            .orElseThrow(() -> new UnauthorizedException("Cluster url not found in kube config"));
        return kubeConfig.getContexts().stream()
            .filter(c -> c.getContext().getCluster().equals(cluster.getName()))
            .findFirst()
            .orElseThrow(() -> new UnauthorizedException("No suitable context found in kube config"));
    }

    private ResourceDeployment createNewResourceDeployment(Deployment deployment, Resource resource,
            Function function, ResourceDeploymentStatus status) {
        FunctionDeployment functionDeployment = new FunctionDeployment();
        functionDeployment.setDeployment(deployment);
        functionDeployment.setResource(resource);
        functionDeployment.setFunction(function);
        functionDeployment.setStatus(status);
        return functionDeployment;
    }

    private ResourceDeployment createNewResourceDeployment(Deployment deployment, Resource resource,
            Service service, String namespace, String context, ResourceDeploymentStatus status) {
        ServiceDeployment serviceDeployment = new ServiceDeployment();
        serviceDeployment.setDeployment(deployment);
        serviceDeployment.setResource(resource);
        serviceDeployment.setService(service);
        serviceDeployment.setStatus(status);
        serviceDeployment.setNamespace(namespace);
        serviceDeployment.setContext(context);
        return serviceDeployment;
    }

    /**
     * Execute the deployment of the resources contained in the deployment.
     *
     * @param deployment the deployment
     * @param accountId the id of the creator of the deployment
     * @param requestDTO the request body
     * @param vpcList the list of vpcs
     */
    // TODO: add check for kubeconfig
    private void initiateDeployment(Deployment deployment, long accountId, DeployResourcesRequest requestDTO,
                                    List<VPC> vpcList) {
        deploymentHandler
            .deployResources(deployment, accountId, requestDTO.getDockerCredentials(), requestDTO.getKubeConfig(),
                vpcList)
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
            .flatMapCompletable(res -> deploymentHandler.terminateResources(deployment, accountId))
            .andThen(Completable.defer(() ->
                resourceDeploymentChecker.submitUpdateStatus(deployment.getDeploymentId(),
                    DeploymentStatusValue.TERMINATED)))
            .doOnError(throwable -> logger.error(throwable.getMessage()))
            .onErrorResumeNext(throwable -> deploymentErrorHandler.onTerminationError(deployment, throwable))
            .subscribe();
    }
}
