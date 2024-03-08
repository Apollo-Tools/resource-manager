package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.deployment.*;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
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
    public void findAllActiveWithAlerting(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<DeploymentAlertingDTO>> findAll = smProvider.withTransactionSingle(sm -> repositoryProvider
                .getDeploymentRepository()
                .findAllActiveWithAlerting(sm)
            )
            .flatMapObservable(Observable::fromIterable)
            .flatMapSingle(deployment -> {
                DeploymentAlertingDTO deploymentAlertingDTO = new DeploymentAlertingDTO();
                deploymentAlertingDTO.setDeploymentId(deployment.getDeploymentId());
                deploymentAlertingDTO.setEnsembleId(deployment.getEnsemble().getEnsembleId());
                deploymentAlertingDTO.setAlertingUrl(deployment.getAlertNotificationUrl());
                // a new session is necessary for each concurrent call
                return smProvider.openSession().map(SessionManager::new)
                    .flatMap(sm -> repositoryProvider.getEnsembleSLORepository()
                    .findAllByEnsembleId(sm, deploymentAlertingDTO.getEnsembleId())
                    .flatMap(ensembleSLOS -> {
                        deploymentAlertingDTO.setEnsembleSLOs(ensembleSLOS);
                        if (ensembleSLOS.isEmpty()) {
                            deploymentAlertingDTO.setResources(List.of());
                            return Single.just(deploymentAlertingDTO);
                        } else {
                            return repositoryProvider.getResourceRepository()
                                .findAllByDeploymentId(sm, deploymentAlertingDTO.getDeploymentId())
                                .flatMapObservable(Observable::fromIterable)
                                .map(resource -> {
                                    resource.setIsLocked(resource.getLockedByDeployment() != null);
                                    if (resource instanceof SubResource) {
                                        return new SubResourceDTO((SubResource) resource);
                                    }
                                    return resource;
                                })
                                .toList()
                                .map(resources -> {
                                    deploymentAlertingDTO.setResources(resources);
                                    return deploymentAlertingDTO;
                                })
                                .flatMap(result -> smProvider.closeSession(sm.getSession())
                                    .andThen(Single.just(result)));
                        }
                    })
                );
            })
            .filter(result -> !result.getEnsembleSLOs().isEmpty())
            .toList();
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
                result.setCreatedAt(deployment.getCreatedAt());
                result.setFinishedAt(deployment.getFinishedAt());
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
        SaveResourceDeploymentUtility resourceDeploymentUtility =
            new SaveResourceDeploymentUtility(accountId, repositoryProvider.getFunctionRepository(),
                repositoryProvider.getServiceRepository());
        LockedResourcesUtility lockUtility = new LockedResourcesUtility(repositoryProvider.getResourceRepository());
        Single<DeployResourcesDTO> save = smProvider.withTransactionSingle(sm -> {
            DeployResourcesDTO deployResources = new DeployResourcesDTO();
            Deployment deployment = new Deployment();
            deployResources.setDeployment(deployment);
            deployResources.setDeploymentCredentials(request.getCredentials());
            deployResources.setVpcList(new ArrayList<>());
            return sm.find(Account.class, accountId)
                .switchIfEmpty(Single.error(new UnauthorizedException()))
                .flatMap(account -> {
                    deployment.setCreatedBy(account);
                    Single<Long> checkResources = Single.just(1L);
                    if (request.getValidation() != null) {
                        // TODO: refactoring into separate class
                        DeploymentValidation validation = request.getValidation();
                        deployment.setAlertNotificationUrl(request.getValidation().getAlertNotificationUrl());
                        checkResources = repositoryProvider.getEnsembleRepository()
                            .findByIdAndAccountId(sm, request.getValidation().getEnsembleId(), accountId)
                            .switchIfEmpty(Single.error(new NotFoundException(Ensemble.class)))
                            .flatMap(ensemble -> {
                                deployment.setEnsemble(ensemble);
                                return repositoryProvider.getResourceRepository()
                                    .findAllByEnsembleId(sm, validation.getEnsembleId());
                            })
                            .flatMapObservable(Observable::fromIterable)
                            .map(Resource::getResourceId)
                            .collect(Collectors.toSet())
                            .flatMap(ensembleResourceIds -> {
                                Observable<Long> serviceResourceIds = Observable.fromIterable(request.getServiceResources())
                                    .map(ServiceResourceIds::getResourceId);
                                Observable<Long> functionResourceIds = Observable
                                    .fromIterable(request.getFunctionResources())
                                    .map(FunctionResourceIds::getResourceId);
                                return Observable.merge(serviceResourceIds, functionResourceIds)
                                    .filter(ensembleResourceIds::contains)
                                    .isEmpty();
                            })
                            .flatMap(requestContainsNonEnsembleResource -> {
                                if (requestContainsNonEnsembleResource) {
                                    return Single.error(new BadInputException("Request contains non ensemble resource"));
                                }
                                return Single.just(1L);
                            });
                    }
                    return checkResources;
                })
                .flatMap(res -> validationUtility.checkDeploymentIsValid(sm, request, deployResources))
                .flatMapCompletable(resources -> repositoryProvider.getStatusRepository()
                    .findOneByStatusValue(sm, DeploymentStatusValue.NEW.name())
                    .switchIfEmpty(Maybe.error(new NotFoundException(ResourceDeploymentStatus.class)))
                    .flatMapCompletable(statusNew -> {
                        Completable saveFunctionDeployments = resourceDeploymentUtility
                            .saveFunctionDeployments(sm, deployment, request, statusNew, resources);
                        Completable saveServiceDeployments = repositoryProvider.getNamespaceRepository()
                            .findAllByAccountIdAndFetch(sm, deployment.getCreatedBy().getAccountId())
                            .flatMapCompletable(namespaces -> resourceDeploymentUtility
                                .saveServiceDeployments(sm, deployment, request, statusNew, namespaces, resources));
                        return Completable.mergeArray(saveFunctionDeployments, saveServiceDeployments);
                    })
                )
                .andThen(Single.defer(() -> lockUtility.lockResources(sm, request.getLockResources(), deployment)))
                .flatMap(lockedResources -> repositoryProvider.getCredentialsRepository()
                    .findAllByAccountId(sm, accountId)
                    .map(credentials -> {
                        deployResources.setCredentialsList(credentials);
                        return deployResources;
                    })
                )
                .flatMap(res -> sm.persist(deployment)
                    .flatMapCompletable(depl -> sm.flush())
                    .andThen(Single.defer(() -> Single.just(-1L)))
                    .flatMap(res2 -> deploymentUtility
                        .mapResourceDeploymentsToDTO(sm, res)
                        .andThen(Single.defer(() -> Single.just(res)))
                    ));
            }
        );

        RxVertxHandler.handleSession(
            save.map(result -> {
                result.getDeployment().setCreatedBy(null);
                result.getDeployment().setServiceDeployments(null);
                result.getDeployment().setFunctionDeployments(null);
                return JsonObject.mapFrom(result);
            }),
            resultHandler
        );
    }

    @Override
    public void handleDeploymentError(long deploymentId, String errorMessage,
            Handler<AsyncResult<Void>> resultHandler) {
        LockedResourcesUtility lockUtility = new LockedResourcesUtility(repositoryProvider.getResourceRepository());
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
            Completable unlockResources = lockUtility.unlockDeploymentResources(sm, deploymentId);
            return Completable.mergeArray(updateStatus, createLog, unlockResources);
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
                    DeploymentStatusValue.DEPLOYED)
                .andThen(Completable.defer(() -> repositoryProvider.getDeploymentRepository()
                    .setDeploymentFinishedTime(sm, request.getDeployment().getDeploymentId())));
            return Completable.mergeArray(setFunctionUrls, setContainerUrls, updateDeploymentStatus)
                .andThen(Completable.defer(sm::flush));
        });

        RxVertxHandler.handleSession(updateDeployment, resultHandler);
    }
}
