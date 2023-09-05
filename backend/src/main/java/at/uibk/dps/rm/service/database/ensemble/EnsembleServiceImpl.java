package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.ensemble.ResourceEnsembleStatus;
import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.EnsembleRepositoryProvider;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.SLOUtility;
import at.uibk.dps.rm.service.database.util.SessionManager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is the implementation of the {@link EnsembleService}.
 *
 * @author matthi-g
 */
public class EnsembleServiceImpl extends DatabaseServiceProxy<Ensemble> implements EnsembleService {

    private final EnsembleRepositoryProvider repositoryProvider;

    private final SLOUtility sloUtility;

    /**
     * Create an instance from the ensembleRepository.
     *
     * @param repositoryProvider the necessary repositories
     */
    public EnsembleServiceImpl(EnsembleRepositoryProvider repositoryProvider, SessionFactory sessionFactory) {
        super(repositoryProvider.getEnsembleRepository(), Ensemble.class, sessionFactory);
        this.repositoryProvider = repositoryProvider;
        this.sloUtility = new SLOUtility(repositoryProvider.getResourceRepository(),
            repositoryProvider.getMetricRepository());
    }

    // TODO: fix return values
    @Override
    public void saveToAccount(long accountId, JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        CreateEnsembleRequest request = data.mapTo(CreateEnsembleRequest.class);
        Single<Ensemble> create = withTransactionSingle(sessionManager ->
            repositoryProvider.getEnsembleRepository().findByNameAndAccountId(sessionManager, request.getName(), accountId)
                .flatMap(existingEnsemble -> Maybe.<Ensemble>error(new AlreadyExistsException(Ensemble.class)))
                .switchIfEmpty(sessionManager.persist(request.getEnsemble(accountId)))
                .flatMap(ensemble -> {
                    Completable createResourceEnsembles = Observable.fromIterable(request.getResources())
                        .map(resourceId -> repositoryProvider.getResourceRepository()
                            .findById(sessionManager, resourceId.getResourceId())
                            .switchIfEmpty(Maybe.error(new NotFoundException(Resource.class)))
                            .flatMapSingle(resource -> {
                                ResourceEnsemble resourceEnsemble = new ResourceEnsemble();
                                resourceEnsemble.setEnsemble(ensemble);
                                resourceEnsemble.setResource(resource);
                                return sessionManager.persist(resourceEnsemble);
                            })
                            .ignoreElement()
                        )
                        .toList()
                        .flatMapCompletable(Completable::merge);
                    Completable createEnsembleSLOs = Observable.fromIterable(request.getServiceLevelObjectives())
                        .map(slo -> {
                            EnsembleSLO ensembleSLO = createEnsembleSLO(slo, ensemble);
                            return sessionManager.persist(ensembleSLO).ignoreElement();
                        })
                        .toList()
                        .flatMapCompletable(Completable::merge);
                    return Completable.mergeArray(createResourceEnsembles, createEnsembleSLOs)
                        .andThen(Single.defer(() -> Single.just(ensemble)));
                })
        );
        RxVertxHandler.handleSession(
            create.map(entity -> {
                entity.setResource_types(null);
                entity.setRegions(null);
                entity.setProviders(null);
                entity.setEnvironments(null);
                entity.setPlatforms(null);
                entity.setCreatedBy(null);
                return JsonObject.mapFrom(entity);
            }),
            resultHandler
        );
    }

    @Override
    public void findAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Ensemble>> findAll = withTransactionSingle(sessionManager -> repositoryProvider
            .getEnsembleRepository()
            .findAll(sessionManager));
        RxVertxHandler.handleSession(
            findAll.map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Ensemble entity: result) {
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            }),
            resultHandler
        );
    }

    @Override
    public void findAllByAccountId(long accountId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Ensemble>> findAll = withTransactionSingle(sessionManager -> repositoryProvider
            .getEnsembleRepository()
            .findAllByAccountId(sessionManager, accountId));
        RxVertxHandler.handleSession(
            findAll.map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Ensemble entity: result) {
                    entity.setResource_types(null);
                    entity.setRegions(null);
                    entity.setProviders(null);
                    entity.setEnvironments(null);
                    entity.setPlatforms(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            }),
            resultHandler
        );
    }

    @Override
    public void findOneByIdAndAccountId(long id, long accountId, Handler<AsyncResult<JsonObject>> resultHandler) {
        Single<GetOneEnsemble> findOne = withTransactionSingle(sessionManager ->
            fetchAndPopulateEnsemble(sessionManager, id, accountId)
        );
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    private Single<GetOneEnsemble> fetchAndPopulateEnsemble(SessionManager sessionManager, long id, long accountId) {
        GetOneEnsemble response = new GetOneEnsemble();
        return repositoryProvider.getEnsembleRepository()
            .findByIdAndAccountId(sessionManager, id, accountId)
            .switchIfEmpty(Single.error(new NotFoundException(Ensemble.class)))
            .flatMap(ensemble -> {
                response.setEnsembleId(ensemble.getEnsembleId());
                response.setName(ensemble.getName());
                response.setRegions(ensemble.getRegions());
                response.setProviders(ensemble.getProviders());
                response.setResourceTypes(ensemble.getResource_types());
                response.setPlatforms(ensemble.getPlatforms());
                response.setEnvironments(ensemble.getEnvironments());
                response.setCreatedAt(ensemble.getCreatedAt());
                response.setUpdatedAt(ensemble.getUpdatedAt());
                return repositoryProvider.getResourceRepository()
                    .findAllByEnsembleId(sessionManager, ensemble.getEnsembleId());
            })
            .flatMapObservable(Observable::fromIterable)
            .map(resource -> {
                if (resource instanceof SubResource) {
                    return new SubResourceDTO((SubResource) resource);
                }
                return resource;
            })
            .toList()
            .flatMap(mappedResources -> {
                response.setResources(mappedResources);
                return repositoryProvider.getEnsembleSLORepository()
                    .findAllByEnsembleId(sessionManager, response.getEnsembleId());
            })
            .map(slos -> {
                response.setServiceLevelObjectives(mapEnsembleSLOstoDTO(slos));
                return response;
            });
    }

    // TODO: check if valid
    @Override
    public void findOne(long id, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<Ensemble> findOne = withTransactionMaybe(sessionManager -> sessionManager.find(Ensemble.class, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(Ensemble.class)))
        );
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    /**
     * Map the ensembleSLOs model entity to the ServiceLevelObjective DTO.
     *
     * @param ensembleSLOs the list of ensembleSLOs
     * @return a List of mapped ServiceLevelObjectives
     */
    private List<ServiceLevelObjective> mapEnsembleSLOstoDTO(List<EnsembleSLO> ensembleSLOs) {
        return ensembleSLOs.stream()
            .map(ensembleSLO -> {
                List<SLOValue> sloValues;
                if (ensembleSLO.getValueNumbers() != null) {
                    sloValues = ensembleSLO.getValueNumbers().stream().map(value -> {
                        SLOValue sloValue = new SLOValue();
                        sloValue.setValueNumber(value);
                        sloValue.setSloValueType(SLOValueType.NUMBER);
                        return sloValue;
                    }).collect(Collectors.toList());
                } else if (ensembleSLO.getValueStrings() != null) {
                    sloValues = ensembleSLO.getValueStrings().stream().map(value -> {
                        SLOValue sloValue = new SLOValue();
                        sloValue.setValueString(value);
                        sloValue.setSloValueType(SLOValueType.STRING);
                        return sloValue;
                    }).collect(Collectors.toList());
                } else {
                    sloValues = ensembleSLO.getValueBools().stream().map(value -> {
                        SLOValue sloValue = new SLOValue();
                        sloValue.setValueBool(value);
                        sloValue.setSloValueType(SLOValueType.BOOLEAN);
                        return sloValue;
                    }).collect(Collectors.toList());
                }
                return new ServiceLevelObjective(ensembleSLO.getName(),
                    ensembleSLO.getExpression(), sloValues);
            }).collect(Collectors.toList());
    }

    private EnsembleSLO createEnsembleSLO(ServiceLevelObjective slo, Ensemble ensemble) {
        EnsembleSLO ensembleSLO = new EnsembleSLO();
        ensembleSLO.setName(slo.getName());
        ensembleSLO.setExpression(slo.getExpression());
        switch (slo.getValue().get(0).getSloValueType()) {
            case NUMBER:
                List<Double> numberValues = slo.getValue().stream()
                    .map(value -> (Double) value.getValueNumber()).collect(Collectors.toList());
                ensembleSLO.setValueNumbers(numberValues);
                break;
            case STRING:
                List<String> stringValues = slo.getValue().stream()
                    .map(SLOValue::getValueString).collect(Collectors.toList());
                ensembleSLO.setValueStrings(stringValues);
                break;
            case BOOLEAN:
                List<Boolean> boolValues = slo.getValue().stream()
                    .map(SLOValue::getValueBool).collect(Collectors.toList());
                ensembleSLO.setValueBools(boolValues);
                break;
        }
        ensembleSLO.setEnsemble(ensemble);
        return ensembleSLO;
    }

    @Override
    public void validateCreateEnsembleRequest(JsonObject data, Handler<AsyncResult<Void>> resultHandler) {
        CreateEnsembleRequest requestDTO = data.mapTo(CreateEnsembleRequest.class);
        List<ResourceId> resourceIds = requestDTO.getResources();
        Completable validateRequest = withTransactionCompletable(sessionManager -> sloUtility
            .findAndFilterResourcesBySLOs(sessionManager, requestDTO)
            .flatMap(resources -> Observable.fromIterable(resourceIds)
                .map(ResourceId::getResourceId)
                .all(resourceId -> resources.stream()
                    .anyMatch(resource -> Objects.equals(resource.getResourceId(), resourceId))
                ))
            .flatMapCompletable(requestFulfillsSLOs -> {
                if (!requestFulfillsSLOs) {
                    return Completable.error(new BadInputException("slo mismatch"));
                }
                return Completable.complete();
            })
        );
        RxVertxHandler.handleSession(validateRequest, resultHandler);
    }

    @Override
    public void validateExistingEnsemble(long accountId, long ensembleId,
            Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<ResourceEnsembleStatus>> validateRequest = withTransactionSingle(sessionManager ->
            validateAndUpdateEnsemble(sessionManager, ensembleId, accountId));
        RxVertxHandler.handleSession(
            validateRequest.map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (ResourceEnsembleStatus status: result) {
                    objects.add(JsonObject.mapFrom(status));
                }
                return new JsonArray(objects);
            }),
            resultHandler
        );
    }

    @Override
    public void validateAllExistingEnsembles(Handler<AsyncResult<Void>> resultHandler) {
        Completable validateAll = withTransactionSingle(sessionManager -> repositoryProvider
            .getEnsembleRepository()
            .findAll(sessionManager))
            .flatMapCompletable(ensembles -> {
                Single<List<ResourceEnsembleStatus>> prevSingle = Single.just(List.of());
                for(Ensemble ensemble : ensembles) {
                    long accountId = ensemble.getCreatedBy().getAccountId();
                    long ensembleId = ensemble.getEnsembleId();
                    prevSingle = prevSingle.flatMap(prev -> withTransactionSingle(sessionManager ->
                        validateAndUpdateEnsemble(sessionManager, ensembleId, accountId)));
                }
                return prevSingle.ignoreElement();
            });
        RxVertxHandler.handleSession(validateAll, resultHandler);
    }

    private Single<List<ResourceEnsembleStatus>> validateAndUpdateEnsemble(SessionManager sessionManager,
            long ensembleId, long accountId) {
        return repositoryProvider.getEnsembleRepository().findByIdAndAccountId(sessionManager, ensembleId, accountId)
            .switchIfEmpty(Single.error(new NotFoundException(Ensemble.class)))
            .flatMap(ensemble -> fetchAndPopulateEnsemble(sessionManager, ensembleId, accountId))
            .flatMap(getOneEnsemble -> sloUtility.findAndFilterResourcesBySLOs(sessionManager, getOneEnsemble)
                .map(validResources -> getResourceEnsembleStatus(validResources, getOneEnsemble.getResources()))
            )
            .flatMap(statusValues -> Observable.fromIterable(statusValues)
                .map(ResourceEnsembleStatus::getIsValid)
                .reduce((status1, status2) -> status1 && status2)
                .flatMapCompletable(status -> repositoryProvider.getEnsembleRepository()
                    .updateValidity(sessionManager, ensembleId, status))
                .andThen(Single.defer(() -> Single.just(statusValues)))
            );
    }

    /**
     * Get the slo status of all resources
     *
     * @param validResources all valid resources
     * @param ensembleResources the resources to validate
     * @return the List of pairs of resource_ids and their validation status being true for valid
     * and false for invalid
     */
    private List<ResourceEnsembleStatus> getResourceEnsembleStatus(List<Resource> validResources,
        List<Resource> ensembleResources) {
        return ensembleResources.stream()
            .map(Resource::getResourceId)
            .map(resourceId -> {
                boolean isValid = validResources.stream()
                    .anyMatch(resource -> Objects.equals(resource.getResourceId(), resourceId));
                return new ResourceEnsembleStatus(resourceId, isValid);
            })
            .collect(Collectors.toList());
    }
}
