package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.EnsembleRepositoryProvider;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
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

    private final EnsembleRepositoryProvider repositoryProvider;

    /**
     * Create an instance from the repositoryProvider.
     *
     * @param repositoryProvider the repository provider
     */
    public ResourceEnsembleServiceImpl(EnsembleRepositoryProvider repositoryProvider,
            SessionManagerProvider smProvider) {
        super(repositoryProvider.getResourceEnsembleRepository(), ResourceEnsemble.class, smProvider);
        this.repositoryProvider = repositoryProvider;
    }

    @Override
    public void saveByEnsembleIdAndResourceId(long accountId, long ensembleId, long resourceId,
            Handler<AsyncResult<JsonObject>> resultHandler) {
        ResourceEnsemble resourceEnsemble = new ResourceEnsemble();
        Single<ResourceEnsemble> create = smProvider.withTransactionSingle(sm -> repositoryProvider
            .getResourceEnsembleRepository()
            .findByEnsembleIdAndResourceId(sm, accountId, ensembleId, resourceId)
            .flatMap(existingResourceEnsemble -> Maybe.<Ensemble>error(new AlreadyExistsException(ResourceEnsemble.class)))
            .switchIfEmpty(repositoryProvider.getEnsembleRepository().findByIdAndAccountId(sm, ensembleId, accountId))
            .switchIfEmpty(Maybe.error(new NotFoundException(Ensemble.class)))
            .flatMap(ensemble -> {
                resourceEnsemble.setEnsemble(ensemble);
                return repositoryProvider.getResourceRepository().findByIdAndFetch(sm, resourceId);
            })
            .switchIfEmpty(Single.error(new NotFoundException(Resource.class)))
            .flatMap(resource -> {
                resourceEnsemble.setResource(resource);
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
        Completable delete = smProvider.withTransactionCompletable(sm -> repositoryProvider
            .getResourceEnsembleRepository()
            .findByEnsembleIdAndResourceId(sm, accountId, ensembleId, resourceId)
            .switchIfEmpty(Maybe.error(new NotFoundException(ResourceEnsemble.class)))
            .flatMapCompletable(sm::remove)
        );
        RxVertxHandler.handleSession(delete, resultHandler);
    }
}
