package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.deployment.*;
import at.uibk.dps.rm.entity.dto.resource.ResourceId;
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
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

import java.util.*;
import java.util.stream.Collectors;

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
            SessionManagerProvider smProvider) {
        super(repositoryProvider.getDeploymentRepository(), Deployment.class, smProvider);
        this.repositoryProvider = repositoryProvider;
    }

    @Override
    public void cancelDeployment(long id, long accountId, Handler<AsyncResult<JsonObject>> resultHandler) {
        DeploymentUtility deploymentUtility = new DeploymentUtility(repositoryProvider);
        TerminateResourcesDTO terminateResources = new TerminateResourcesDTO();
        terminateResources.setFunctionDeployments(new ArrayList<>());
        terminateResources.setServiceDeployments(new ArrayList<>());
        Single<TerminateResourcesDTO> update = smProvider.withTransactionSingle(sm -> repositoryProvider
            .getDeploymentRepository()
            .findByIdAndAccountId(sm, id, accountId)
            .switchIfEmpty(Single.error(new NotFoundException(Deployment.class)))
            .flatMap(deployment -> {
                terminateResources.setDeployment(deployment);
                return repositoryProvider.getResourceDeploymentRepository()
                    .findAllByDeploymentIdAndFetch(sm, id)
                    .flatMapCompletable(resourceDeployments -> {
                        long deployedAmount = resourceDeployments.stream().filter(resourceDeployment ->
                            DeploymentStatusValue.fromDeploymentStatus(resourceDeployment.getStatus())
                                .equals(DeploymentStatusValue.DEPLOYED))
                            .count();
                        if (resourceDeployments.isEmpty() || deployedAmount != resourceDeployments.size()) {
                            return Completable.error(new BadInputException("invalid deployment state"));
                        }
                        return repositoryProvider.getStatusRepository()
                            .findOneByStatusValue(sm, DeploymentStatusValue.TERMINATING.getValue())
                            .flatMapCompletable(status -> Completable.fromAction(() -> resourceDeployments
                                .forEach(resourceDeployment -> resourceDeployment.setStatus(status)))
                            );
                    })
                    .andThen(repositoryProvider.getCredentialsRepository().findAllByAccountId(sm, accountId))
                    .flatMapCompletable(credentials -> {
                        terminateResources.setCredentialsList(credentials);
                        return deploymentUtility.mapResourceDeploymentsToDTO(sm, terminateResources);
                    })
                    .andThen(Single.defer(() -> Single.just(terminateResources)));
            })
        );
        RxVertxHandler.handleSession(update.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void findAllByAccountId(long accountId, Handler<AsyncResult<JsonArray>> resultHandler) {
        DeploymentUtility deploymentUtility = new DeploymentUtility(repositoryProvider);
        Single<List<DeploymentResponse>> findAll = smProvider.withTransactionSingle(sm -> repositoryProvider
            .getDeploymentRepository()
            .findAllByAccountId(sm, accountId)
            .flatMapObservable(Observable::fromIterable)
            .flatMapSingle(deployment -> deploymentUtility.composeDeploymentResponse(sm, deployment))
            .toList()
        );
        RxVertxHandler.handleSession(findAll.map(this::mapResultListToJsonArray), resultHandler);
    }

    @Override
    public void findOneByIdAndAccountId(long id, long accountId, Handler<AsyncResult<JsonObject>> resultHandler) {
        DeploymentWithResourcesDTO result = new DeploymentWithResourcesDTO();
        Single<DeploymentWithResourcesDTO> findOne = smProvider.withTransactionSingle(sm -> repositoryProvider
            .getDeploymentRepository()
            .findByIdAndAccountId(sm, id, accountId)
            .switchIfEmpty(Single.error(new NotFoundException(Deployment.class)))
            .flatMap(deployment -> {
                result.setDeploymentId(id);
                result.setIsActive(deployment.getIsActive());
                result.setCreatedAt(deployment.getCreatedAt());
                return repositoryProvider.getFunctionDeploymentRepository().findAllByDeploymentId(sm, id);
            })
            .flatMap(functionDeployments -> {
                result.setFunctionResources(functionDeployments);
                return repositoryProvider.getServiceDeploymentRepository().findAllByDeploymentId(sm, id);
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
        DeploymentValidationUtility validationUtility = new DeploymentValidationUtility(accountId, repositoryProvider);
        DeploymentUtility deploymentUtility = new DeploymentUtility(repositoryProvider);
        DeployResourcesRequest request = data.mapTo(DeployResourcesRequest.class);
        SaveResourceDeploymentUtility resourceDeploymentUtility = new SaveResourceDeploymentUtility();
        DeployResourcesDTO deployResources = new DeployResourcesDTO();
        Deployment deployment = new Deployment();
        deployResources.setDeployment(deployment);
        deployResources.setDeploymentCredentials(request.getCredentials());
        deployResources.setVpcList(new ArrayList<>());
        List<Long> lockResourcesId = request.getLockResources().stream()
            .map(ResourceId::getResourceId)
            .collect(Collectors.toList());
        Single<DeployResourcesDTO> save = smProvider.withTransactionSingle(sm -> sm.find(Account.class, accountId)
            .switchIfEmpty(Maybe.error(new UnauthorizedException()))
            .flatMapCompletable(account -> {
                deployment.setIsActive(true);
                deployment.setCreatedBy(account);
                return sm.persist(deployment)
                    .flatMapCompletable(res -> sm.flush());
            })
            .andThen(Single.defer(() -> validationUtility.checkDeploymentIsValid(sm, request, deployResources)))
            .flatMapCompletable(resources -> repositoryProvider.getStatusRepository()
                .findOneByStatusValue(sm, DeploymentStatusValue.NEW.name())
                .switchIfEmpty(Maybe.error(new NotFoundException(ResourceDeploymentStatus.class)))
                .flatMapCompletable(statusNew -> {
                    Completable saveFunctionDeployments = resourceDeploymentUtility
                        .saveFunctionDeployments(sm, deployment, request, statusNew, resources);
                    Completable saveServiceDeployments = repositoryProvider.getNamespaceRepository()
                        .findAllByAccountIdAndFetch(sm, deployment.getCreatedBy().getAccountId())
                        .flatMapCompletable(namespaces -> resourceDeploymentUtility
                            .saveServiceDeployments(sm, deployment, request, statusNew, namespaces,
                                resources));
                    return Completable.mergeArray(saveFunctionDeployments, saveServiceDeployments);
                })
            )
            .andThen(Completable.defer(() -> repositoryProvider.getResourceRepository()
                .findAllByResourceIdsAndFetch(sm, lockResourcesId)
                .flatMapObservable(Observable::fromIterable)
                .flatMapCompletable(resource -> {
                    if (!resource.getIsLockable()) {
                        return Completable.error(new BadInputException("resource " + resource + " is not lockable"));
                    }
                    resource.setLockedByDeployment(deployment);
                    return  sm.flush();
                })
            ))
            .andThen(Single.defer(() -> repositoryProvider.getCredentialsRepository()
                .findAllByAccountId(sm, accountId)
                .map(credentials -> {
                    deployResources.setCredentialsList(credentials);
                    return deployResources;
                })
            ))
        )
        .flatMap(deployResourcesDTO -> smProvider.withTransactionSingle(sm -> deploymentUtility
            .mapResourceDeploymentsToDTO(sm, deployResourcesDTO)
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

    @Override
    public void handleDeploymentError(long deploymentId, String errorMessage,
            Handler<AsyncResult<Void>> resultHandler) {
        Completable handleError = smProvider.withTransactionCompletable(sm -> {
            Completable updateStatus = repositoryProvider.getResourceDeploymentRepository()
                .updateDeploymentStatusByDeploymentId(sm, deploymentId, DeploymentStatusValue.ERROR);
            Log log = new Log();
            log.setLogValue(errorMessage);
            DeploymentLog deploymentLog = new DeploymentLog();
            deploymentLog.setLog(log);
            Deployment deployment = new Deployment();
            deployment.setDeploymentId(deploymentId);
            deploymentLog.setDeployment(deployment);
            Completable createLog = sm.persist(log)
                .flatMap(res -> sm.persist(deploymentLog))
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
        Completable updateDeployment = smProvider.withTransactionCompletable(sm -> {
            Completable setFunctionUrls = urlUtility.setTriggerUrlsForFunctions(sm, deploymentOutput, request);
            Completable setContainerUrls = urlUtility.setTriggerUrlForContainers(sm, request);
            Completable updateDeploymentStatus = repositoryProvider.getResourceDeploymentRepository()
                .updateDeploymentStatusByDeploymentId(sm, request.getDeployment().getDeploymentId(),
                    DeploymentStatusValue.DEPLOYED);
            return Completable.mergeArray(setFunctionUrls, setContainerUrls, updateDeploymentStatus);
        });

        RxVertxHandler.handleSession(updateDeployment, resultHandler);
    }
}
