package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.ensemble.ResourceEnsembleStatus;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.EnsembleRepositoryProvider;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is the implementation of the {@link EnsembleService}.
 *
 * @author matthi-g
 */
public class EnsembleServiceImpl extends DatabaseServiceProxy<Ensemble> implements EnsembleService {

    private final EnsembleRepositoryProvider repositoryProvider;

    /**
     * Create an instance from the ensembleRepository.
     *
     * @param repositoryProvider the necessary repositories
     */
    public EnsembleServiceImpl(EnsembleRepositoryProvider repositoryProvider, SessionManagerProvider smProvider) {
        super(repositoryProvider.getEnsembleRepository(), Ensemble.class, smProvider);
        this.repositoryProvider = repositoryProvider;
    }

    @Override
    public void findAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Ensemble>> findAll = smProvider.withTransactionSingle(sm -> repositoryProvider
            .getEnsembleRepository()
            .findAllAndFetch(sm));
        RxVertxHandler.handleSession(findAll.map(this::mapResultListToJsonArray), resultHandler);
    }

    @Override
    public void findAllByAccountId(long accountId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Ensemble>> findAll = smProvider.withTransactionSingle(sm -> repositoryProvider
            .getEnsembleRepository()
            .findAllByAccountId(sm, accountId));
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
        EnsembleUtility ensembleUtility = new EnsembleUtility(repositoryProvider);
        Single<GetOneEnsemble> findOne = smProvider.withTransactionSingle(sm ->
            ensembleUtility.fetchAndPopulateEnsemble(sm, id, accountId)
        );
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void findOne(long id, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<Ensemble> findOne = smProvider.withTransactionMaybe( sm -> sm.find(Ensemble.class, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(Ensemble.class)))
        );
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void saveToAccount(long accountId, JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        CreateEnsembleRequest request = data.mapTo(CreateEnsembleRequest.class);
        Single<Ensemble> create = smProvider.withTransactionSingle(sm -> repositoryProvider
            .getEnsembleRepository().findByNameAndAccountId(sm, request.getName(), accountId)
            .flatMap(existingEnsemble -> Maybe.error(new AlreadyExistsException(Ensemble.class)))
            .switchIfEmpty(sm.find(Account.class, accountId))
            .switchIfEmpty(Single.error(new NotFoundException(Account.class)))
            .flatMap(account -> sm.persist(request.getEnsemble(accountId)))
            .flatMap(ensemble -> {
                Completable createResourceEnsembles = Observable.fromIterable(request.getResources())
                    .map(resourceId -> sm.find(Resource.class, resourceId.getResourceId())
                        .switchIfEmpty(Maybe.error(new NotFoundException(Resource.class)))
                        .flatMapSingle(resource -> {
                            ResourceEnsemble resourceEnsemble = new ResourceEnsemble();
                            resourceEnsemble.setEnsemble(ensemble);
                            resourceEnsemble.setResource(resource);
                            return sm.persist(resourceEnsemble);
                        })
                        .ignoreElement()
                    )
                    .toList()
                    .flatMapCompletable(Completable::merge);
                Completable createEnsembleSLOs = Observable.fromIterable(request.getServiceLevelObjectives())
                    .map(slo -> {
                        EnsembleSLO ensembleSLO = EnsembleUtility.createEnsembleSLO(slo, ensemble);
                        return sm.persist(ensembleSLO).ignoreElement();
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
    public void updateEnsembleStatus(long ensembleId, JsonArray statusValues,
            Handler<AsyncResult<Void>> resultHandler) {
        EnsembleValidationUtility validationUtility = new EnsembleValidationUtility(repositoryProvider);
        List<ResourceEnsembleStatus> statusValueList = statusValues.stream()
            .map(statusValue -> ((JsonObject) statusValue).mapTo(ResourceEnsembleStatus.class))
            .collect(Collectors.toList());
        Completable validateRequest = smProvider.withTransactionCompletable(sm ->
            validationUtility.updateResourceEnsembleStatuses(sm, ensembleId, statusValueList));
        RxVertxHandler.handleSession(validateRequest, resultHandler);
    }

    @Override
    public void updateEnsembleStatusMap(Map<String, JsonArray> statusValues, Handler<AsyncResult<Void>> resultHandler) {
        EnsembleValidationUtility validationUtility = new EnsembleValidationUtility(repositoryProvider);
        Completable validateRequest = smProvider.withTransactionCompletable(sm -> Observable
            .fromIterable(statusValues.entrySet())
            .flatMapCompletable(entrySet -> Observable.fromIterable(entrySet.getValue())
                .map(statusValue -> ((JsonObject) statusValue).mapTo(ResourceEnsembleStatus.class))
                .collect(Collectors.toList())
                .flatMapCompletable(statusValueList -> validationUtility.updateResourceEnsembleStatuses(sm,
                    Long.parseLong(entrySet.getKey()), statusValueList)))
        );
        RxVertxHandler.handleSession(validateRequest, resultHandler);
    }

    @Override
    public void validateExistingEnsemble(long accountId, long ensembleId,
            Handler<AsyncResult<JsonArray>> resultHandler) {
        EnsembleValidationUtility validationUtility = new EnsembleValidationUtility(repositoryProvider);
        Single<List<ResourceEnsembleStatus>> validateRequest = smProvider.withTransactionSingle(sm ->
            validationUtility.validateAndUpdateEnsemble(sm, ensembleId, accountId));
        RxVertxHandler.handleSession(validateRequest.map(this::mapResultListToJsonArray), resultHandler);
    }

    @Override
    public void validateAllExistingEnsembles(Handler<AsyncResult<Void>> resultHandler) {
        EnsembleValidationUtility validationUtility = new EnsembleValidationUtility(repositoryProvider);
        Completable validateAll = smProvider.withTransactionSingle(sm -> repositoryProvider.getEnsembleRepository()
            .findAll(sm))
            .flatMapCompletable(ensembles -> {
                Single<List<ResourceEnsembleStatus>> prevSingle = Single.just(List.of());
                for(Ensemble ensemble : ensembles) {
                    long accountId = ensemble.getCreatedBy().getAccountId();
                    long ensembleId = ensemble.getEnsembleId();
                    prevSingle = prevSingle.flatMap(prev -> smProvider.withTransactionSingle(sm ->
                        validationUtility.validateAndUpdateEnsemble(sm, ensembleId, accountId)));
                }
                return prevSingle.ignoreElement();
            });
        RxVertxHandler.handleSession(validateAll, resultHandler);
    }
}
