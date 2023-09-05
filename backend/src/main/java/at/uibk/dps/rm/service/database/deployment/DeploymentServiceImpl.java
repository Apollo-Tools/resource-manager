package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.deployment.*;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.DeploymentRepositoryProvider;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.*;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.*;

/**
 * This is the implementation of the {@link DeploymentService}.
 *
 * @author matthi-g
 */
public class DeploymentServiceImpl extends DatabaseServiceProxy<Deployment> implements DeploymentService {

    private final DeploymentRepositoryProvider repositoryProvider;

    /**
     * Create an instance from the repository provider
     *
     * @param repositoryProvider the necessary repositories
     */
    public DeploymentServiceImpl(DeploymentRepositoryProvider repositoryProvider,
            SessionFactory sessionFactory) {
        super(repositoryProvider.getDeploymentRepository(), Deployment.class, sessionFactory);
        this.repositoryProvider = repositoryProvider;
    }

    @Override
    public void cancelDeployment(long id, long accountId, Handler<AsyncResult<JsonObject>> resultHandler) {
        TerminateResourcesDTO terminateResources = new TerminateResourcesDTO();
        terminateResources.setFunctionDeployments(new ArrayList<>());
        terminateResources.setServiceDeployments(new ArrayList<>());
        Single<TerminateResourcesDTO> update = withTransactionSingle(sessionManager -> repositoryProvider
            .getDeploymentRepository()
            .findByIdAndAccountId(sessionManager, id, accountId)
            .switchIfEmpty(Single.error(new NotFoundException(Deployment.class)))
            .flatMap(deployment -> {
                terminateResources.setDeployment(deployment);
                return repositoryProvider.getResourceDeploymentRepository()
                    .findAllByDeploymentIdAndFetch(sessionManager, id)
                    .flatMapCompletable(resourceDeployments -> {
                        long deployedAmount = resourceDeployments.stream().filter(resourceDeployment ->
                            DeploymentStatusValue.fromDeploymentStatus(resourceDeployment.getStatus())
                                .equals(DeploymentStatusValue.DEPLOYED))
                            .count();
                        if (resourceDeployments.isEmpty() || deployedAmount != resourceDeployments.size()) {
                            return Completable.error(new BadInputException("invalid deployment state"));
                        }
                        return repositoryProvider.getStatusRepository()
                            .findOneByStatusValue(sessionManager, DeploymentStatusValue.TERMINATING.getValue())
                            .flatMapCompletable(status -> Completable.fromAction(() -> resourceDeployments
                                .forEach(resourceDeployment -> resourceDeployment.setStatus(status)))
                            );
                    })
                    .andThen(repositoryProvider.getCredentialsRepository()
                        .findAllByAccountId(sessionManager, accountId))
                    .flatMapCompletable(credentials -> {
                        terminateResources.setCredentialsList(credentials);
                        return mapResourceDeploymentsToDTO(sessionManager, terminateResources);
                    })
                    .andThen(Single.defer(() -> Single.just(terminateResources)));
            })
        );
        RxVertxHandler.handleSession(update.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void findAllByAccountId(long accountId, Handler<AsyncResult<JsonArray>> resultHandler) {
        List<DeploymentResponse> deploymentResponses = new ArrayList<>();
        Single<List<DeploymentResponse>> findAll = withTransactionSingle(sessionManager -> repositoryProvider
            .getDeploymentRepository()
            .findAllByAccountId(sessionManager, accountId)
            .flatMapObservable(Observable::fromIterable)
            .flatMapCompletable(deployment -> composeDeploymentResponse(sessionManager, deployment, deploymentResponses))
            .andThen(Single.defer(() -> Single.just(deploymentResponses)))
        );
        RxVertxHandler.handleSession(
            findAll.map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (DeploymentResponse entity: result) {
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            }),
            resultHandler
        );
    }

    private Completable composeDeploymentResponse(SessionManager sessionManager, Deployment deployment,
            List<DeploymentResponse> deploymentResponses) {
        DeploymentResponse deploymentResponse = new DeploymentResponse();
        deploymentResponse.setDeploymentId(deployment.getDeploymentId());
        deploymentResponse.setCreatedAt(deployment.getCreatedAt());
        deploymentResponses.add(deploymentResponse);
        return repositoryProvider.getResourceDeploymentRepository()
            .findAllByDeploymentIdAndFetch(sessionManager, deployment.getDeploymentId())
            .flatMapCompletable(resourceDeployments -> Completable.fromAction(() -> {
                DeploymentStatusValue crucialDeploymentStatus = DeploymentStatusUtility
                    .checkCrucialResourceDeploymentStatus(resourceDeployments);
                deploymentResponse.setStatusValue(crucialDeploymentStatus);
            }));
    }

    @Override
    public void findOneByIdAndAccountId(long id, long accountId, Handler<AsyncResult<JsonObject>> resultHandler) {
        DeploymentWithResourcesDTO result = new DeploymentWithResourcesDTO();
        Single<DeploymentWithResourcesDTO> findOne = withTransactionSingle(sessionManager -> repositoryProvider
            .getDeploymentRepository()
            .findByIdAndAccountId(sessionManager, id, accountId)
            .switchIfEmpty(Single.error(new NotFoundException(Deployment.class)))
            .flatMap(deployment -> {
                result.setDeploymentId(id);
                result.setIsActive(deployment.getIsActive());
                result.setCreatedAt(deployment.getCreatedAt());
                return repositoryProvider.getFunctionDeploymentRepository().findAllByDeploymentId(sessionManager, id);
            })
            .flatMap(functionDeployments -> {
                result.setFunctionResources(functionDeployments);
                return repositoryProvider.getServiceDeploymentRepository().findAllByDeploymentId(sessionManager, id);
            })
            .map(serviceDeployments -> {
                result.setServiceResources(serviceDeployments);
                return result;
            })
        );
        RxVertxHandler.handleSession(findOne
            .map(foundDeployment -> {
                foundDeployment.getFunctionResources().forEach(resourceDeployment ->
                    resourceDeployment.setDeployment(null));
                foundDeployment.getServiceResources().forEach(resourceDeployment ->
                    resourceDeployment.setDeployment(null));
                return JsonObject.mapFrom(foundDeployment);
            }),
            resultHandler
        );
    }

    @Override
    public void saveToAccount(long accountId, JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        DeploymentValidationUtility validator = new DeploymentValidationUtility(accountId, repositoryProvider);
        DeployResourcesRequest request = data.mapTo(DeployResourcesRequest.class);
        SaveResourceDeploymentUtility resourceDeploymentUtility = new SaveResourceDeploymentUtility(repositoryProvider);
        DeployResourcesDTO deployResources = new DeployResourcesDTO();
        Deployment deployment = new Deployment();
        deployResources.setDeployment(deployment);
        deployResources.setDeploymentCredentials(request.getCredentials());
        deployResources.setVpcList(new ArrayList<>());
        Single<DeployResourcesDTO> save = withTransactionSingle(sessionManager -> repositoryProvider
            .getAccountRepository()
            .findById(sessionManager, accountId)
            .switchIfEmpty(Maybe.error(new UnauthorizedException()))
            .flatMapCompletable(account -> {
                deployment.setIsActive(true);
                deployment.setCreatedBy(account);
                return sessionManager.persist(deployment)
                    .flatMapCompletable(res -> sessionManager.flush());
            })
            .andThen(Single.defer(() -> validator.checkDeploymentIsValid(sessionManager, request, deployResources)))
            .flatMapCompletable(resources -> repositoryProvider.getStatusRepository()
                .findOneByStatusValue(sessionManager, DeploymentStatusValue.NEW.name())
                .switchIfEmpty(Maybe.error(new NotFoundException(ResourceDeploymentStatus.class)))
                .flatMapCompletable(statusNew -> {
                    Completable saveFunctionDeployments = resourceDeploymentUtility
                        .saveFunctionDeployments(sessionManager, deployment, request, statusNew, resources);
                    Completable saveServiceDeployments = repositoryProvider.getNamespaceRepository()
                        .findAllByAccountIdAndFetch(sessionManager, deployment.getCreatedBy().getAccountId())
                        .flatMapCompletable(namespaces -> resourceDeploymentUtility
                            .saveServiceDeployments(sessionManager, deployment, request, statusNew, namespaces,
                                resources));
                    return Completable.mergeArray(saveFunctionDeployments, saveServiceDeployments);
                })
            )
            .andThen(Single.defer(() -> repositoryProvider.getCredentialsRepository()
                .findAllByAccountId(sessionManager, accountId)
                .map(credentials -> {
                    deployResources.setCredentialsList(credentials);
                    return deployResources;
                })
            ))
        )
        .flatMap(deployResourcesDTO -> withTransactionSingle(session ->
            mapResourceDeploymentsToDTO(session, deployResourcesDTO)
                .andThen(Single.defer(() -> Single.just(deployResources)))
            )
        );

        RxVertxHandler.handleSession(
            save.map(result -> {
                result.getDeployment().setCreatedBy(null);
                return JsonObject.mapFrom(result);
            }),
            resultHandler
        );
    }

    /**
     * Map resource deployments to a deploy/terminate dto.
     *
     * @param sessionManager the database session manager
     * @param deployTerminateDTO the request
     */
    private Completable mapResourceDeploymentsToDTO(SessionManager sessionManager,
            DeployTerminateDTO deployTerminateDTO) {
        long deploymentId = deployTerminateDTO.getDeployment().getDeploymentId();
        return repositoryProvider.getFunctionDeploymentRepository()
            .findAllByDeploymentId(sessionManager, deploymentId)
            .flatMap(functionDeployments -> {
                deployTerminateDTO.setFunctionDeployments(functionDeployments);
                return repositoryProvider.getServiceDeploymentRepository().findAllByDeploymentId(sessionManager,
                    deploymentId);
            })
            .flatMapObservable(Observable::fromIterable)
            .flatMapSingle(serviceDeployment -> sessionManager.fetch(serviceDeployment.getService().getEnvVars())
                .flatMap(envVars -> sessionManager.fetch(serviceDeployment.getService().getVolumeMounts()))
                .map(result -> serviceDeployment)
            )
            .toList()
            .flatMapCompletable(serviceDeployments -> {
                deployTerminateDTO.setServiceDeployments(serviceDeployments);
                return Completable.complete();
            });
    }

    @Override
    public void handleDeploymentError(long id, String errorMessage, Handler<AsyncResult<Void>> resultHandler) {
        Completable handleError = withTransactionCompletable(sessionManager -> {
            Completable updateStatus = repositoryProvider.getResourceDeploymentRepository()
                .updateDeploymentStatusByDeploymentId(sessionManager, id, DeploymentStatusValue.ERROR);
            Log log = new Log();
            log.setLogValue(errorMessage);
            DeploymentLog deploymentLog = new DeploymentLog();
            deploymentLog.setLog(log);
            Deployment deployment = new Deployment();
            deployment.setDeploymentId(id);
            deploymentLog.setDeployment(deployment);
            Completable createLog = sessionManager.persist(log)
                .flatMap(res -> sessionManager.persist(deploymentLog))
                .ignoreElement();
            return Completable.mergeArray(updateStatus, createLog);
        });
        RxVertxHandler.handleSession(handleError, resultHandler);
    }

    @Override
    public void handleDeploymentSuccessful(JsonObject terraformOutput, DeployResourcesDTO request,
            Handler<AsyncResult<Void>> resultHandler) {
        TriggerUrlUtility urlUtility = new TriggerUrlUtility(repositoryProvider);
        DeploymentOutput deploymentOutput = DeploymentOutput.fromJson(terraformOutput);
        Completable updateDeployment = withTransactionCompletable(sessionManager -> {
            Completable setFunctionUrls = urlUtility.setTriggerUrlsForFunctions(sessionManager, deploymentOutput,
                request);
            Completable setContainerUrls = urlUtility.setTriggerUrlForContainers(sessionManager, request);
            Completable updateDeploymentStatus = repositoryProvider.getResourceDeploymentRepository()
                .updateDeploymentStatusByDeploymentId(sessionManager, request.getDeployment().getDeploymentId(),
                    DeploymentStatusValue.DEPLOYED);
            return Completable.mergeArray(setFunctionUrls, setContainerUrls, updateDeploymentStatus);
        });

        RxVertxHandler.handleSession(updateDeployment, resultHandler);
    }
}
