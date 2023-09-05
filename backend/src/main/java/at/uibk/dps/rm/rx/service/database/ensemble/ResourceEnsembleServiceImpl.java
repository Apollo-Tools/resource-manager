package at.uibk.dps.rm.rx.service.database.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.rx.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.rx.repository.ensemble.EnsembleSLORepository;
import at.uibk.dps.rm.rx.repository.ensemble.ResourceEnsembleRepository;
import at.uibk.dps.rm.rx.repository.resource.ResourceRepository;
import at.uibk.dps.rm.rx.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.ServiceLevelObjectiveMapper;
import at.uibk.dps.rm.util.validation.SLOCompareUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

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
            ResourceRepository resourceRepository, Stage.SessionFactory sessionFactory) {
        super(repository, ResourceEnsemble.class, sessionFactory);
        this.repository = repository;
        this.ensembleSLORepository = ensembleSLORepository;
        this.ensembleRepository = ensembleRepository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void saveByEnsembleIdAndResourceId(long accountId, long ensembleId, long resourceId,
            Handler<AsyncResult<JsonObject>> resultHandler) {
        ResourceEnsemble resourceEnsemble = new ResourceEnsemble();
        Single<ResourceEnsemble> create = withTransactionSingle(sessionManager -> repository
            .findByEnsembleIdAndResourceId(sessionManager, accountId, ensembleId, resourceId)
            .flatMap(existingResourceEnsemble -> Maybe.<Ensemble>error(new AlreadyExistsException(ResourceEnsemble.class)))
            .switchIfEmpty(ensembleRepository.findByIdAndAccountId(sessionManager, ensembleId, accountId))
            .switchIfEmpty(Maybe.error(new NotFoundException(Ensemble.class)))
            .flatMap(ensemble -> {
                resourceEnsemble.setEnsemble(ensemble);
                return resourceRepository.findByIdAndFetch(sessionManager, resourceId);
            })
            .switchIfEmpty(Single.error(new NotFoundException(Resource.class)))
            .flatMap(resource -> {
                resourceEnsemble.setResource(resource);
                return ensembleSLORepository.findAllByEnsembleId(sessionManager, ensembleId);
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
                return sessionManager.persist(resourceEnsemble);
            })
        );
        handleSession(
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
        Completable delete = withTransactionCompletable(sessionManager -> repository
            .findByEnsembleIdAndResourceId(sessionManager, accountId, ensembleId, resourceId)
            .switchIfEmpty(Maybe.error(new NotFoundException(ResourceEnsemble.class)))
            .flatMapCompletable(sessionManager::remove)
        );
        handleSession(delete, resultHandler);
    }
}
