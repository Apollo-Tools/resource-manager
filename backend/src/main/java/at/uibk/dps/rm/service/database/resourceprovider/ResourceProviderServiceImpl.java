package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

import java.util.List;

/**
 * This is the implementation of the {@link ResourceProviderService}.
 *
 * @author matthi-g
 */
public class ResourceProviderServiceImpl extends DatabaseServiceProxy<ResourceProvider> implements ResourceProviderService {

    private final ResourceProviderRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the resource provider repository
     */
    public ResourceProviderServiceImpl(ResourceProviderRepository repository, SessionManagerProvider smProvider) {
        super(repository, ResourceProvider.class, smProvider);
        this.repository = repository;
    }

    @Override
    public void findOne(long id, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<ResourceProvider> findOne = smProvider.withTransactionMaybe( sm -> repository.findByIdAndFetch(sm, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(ResourceProvider.class)))
        );
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void findAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<ResourceProvider>> findAll = smProvider.withTransactionSingle(repository::findAllAndFetch);
        RxVertxHandler.handleSession(findAll.map(this::mapResultListToJsonArray), resultHandler);
    }
}
