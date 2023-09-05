package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.util.RxVertxHandler;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
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
    public ResourceProviderServiceImpl(ResourceProviderRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, ResourceProvider.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public void findOne(long id, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<ResourceProvider> findOne = withTransactionMaybe(sessionManager -> repository
            .findByIdAndFetch(sessionManager, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(ResourceProvider.class)))
        );
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void findAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<ResourceProvider>> findAll = withTransactionSingle(repository::findAllAndFetch);
        RxVertxHandler.handleSession(
            findAll.map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (ResourceProvider entity: result) {
                    entity.setProviderPlatforms(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            }),
            resultHandler
        );
    }
}
