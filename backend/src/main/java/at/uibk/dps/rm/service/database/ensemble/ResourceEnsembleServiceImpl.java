package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.repository.ensemble.EnsembleSLORepository;
import at.uibk.dps.rm.repository.ensemble.ResourceEnsembleRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import at.uibk.dps.rm.util.misc.ServiceLevelObjectiveMapper;
import at.uibk.dps.rm.util.validation.SLOCompareUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

/**
 * This is the implementation of the {@link ResourceEnsembleService}.
 *
 * @author matthi-g
 */
public class ResourceEnsembleServiceImpl  extends DatabaseServiceProxy<ResourceEnsemble> implements
    ResourceEnsembleService {

    private final ResourceEnsembleRepository repository;

    private final EnsembleSLORepository ensembleSLORepository;

    private final EnsembleRepository ensembleRepository;

    private final ResourceRepository resourceRepository;

    /**
     * Create an instance from the resourceEnsembleRepository, ensembleSLORepository,
     * ensembleRepository, resourceRepository.
     *
     * @param repository the resource ensemble repository
     * @param ensembleSLORepository the ensemble slo repository
     * @param ensembleRepository  the ensemble repository
     * @param resourceRepository  the resource repository
     */
    public ResourceEnsembleServiceImpl(ResourceEnsembleRepository repository,
            EnsembleSLORepository ensembleSLORepository, EnsembleRepository ensembleRepository,
            ResourceRepository resourceRepository, SessionManagerProvider smProvider) {
        super(repository, ResourceEnsemble.class, smProvider);
        this.repository = repository;
        this.ensembleSLORepository = ensembleSLORepository;
        this.ensembleRepository = ensembleRepository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void saveByEnsembleIdAndResourceId(long accountId, long ensembleId, long resourceId,
            Handler<AsyncResult<JsonObject>> resultHandler) {
        ResourceEnsemble resourceEnsemble = new ResourceEnsemble();
        Single<ResourceEnsemble> create = smProvider.withTransactionSingle(sm -> repository
            .findByEnsembleIdAndResourceId(sm, accountId, ensembleId, resourceId)
            .flatMap(existingResourceEnsemble -> Maybe.<Ensemble>error(new AlreadyExistsException(ResourceEnsemble.class)))
            .switchIfEmpty(ensembleRepository.findByIdAndAccountId(sm, ensembleId, accountId))
            .switchIfEmpty(Maybe.error(new NotFoundException(Ensemble.class)))
            .flatMap(ensemble -> {
                resourceEnsemble.setEnsemble(ensemble);
                return resourceRepository.findByIdAndFetch(sm, resourceId);
            })
            .switchIfEmpty(Single.error(new NotFoundException(Resource.class)))
            .flatMap(resource -> {
                resourceEnsemble.setResource(resource);
                return ensembleSLORepository.findAllByEnsembleId(sm, ensembleId);
            })
            .flatMapObservable(Observable::fromIterable)
            .map(ServiceLevelObjectiveMapper::mapEnsembleSLO)
            .toList()
            .flatMap(slos -> {
                boolean isValidByMetrics = SLOCompareUtility
                    .resourceFilterBySLOValueType(resourceEnsemble.getResource(), slos);
                boolean isValidByNonMetrics = SLOCompareUtility
                    .resourceValidByNonMetricSLOS(resourceEnsemble.getResource(), resourceEnsemble.getEnsemble());
                if (!isValidByMetrics || !isValidByNonMetrics) {
                    return Single.error(new BadInputException("resource does not fulfill service level objectives"));
                }
                return sm.persist(resourceEnsemble);
            })
        );
        RxVertxHandler.handleSession(
            create.map(result -> {
                JsonObject response = new JsonObject();
                response.put("ensemble_id", result.getEnsemble().getEnsembleId());
                response.put("resource_id", result.getResource().getResourceId());
                return response;
            }),
            resultHandler
        );
    }

    @Override
    public void deleteByEnsembleIdAndResourceId(long accountId, long ensembleId, long resourceId,
            Handler<AsyncResult<Void>> resultHandler) {
        Completable delete = smProvider.withTransactionCompletable(sm -> repository
            .findByEnsembleIdAndResourceId(sm, accountId, ensembleId, resourceId)
            .switchIfEmpty(Maybe.error(new NotFoundException(ResourceEnsemble.class)))
            .flatMapCompletable(sm::remove)
        );
        RxVertxHandler.handleSession(delete, resultHandler);
    }
}
