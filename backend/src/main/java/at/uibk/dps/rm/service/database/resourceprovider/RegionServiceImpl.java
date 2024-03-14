package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
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
 * This is the implementation of the {@link RegionService}.
 *
 * @author matthi-g
 */
public class RegionServiceImpl extends DatabaseServiceProxy<Region> implements RegionService {
    private final RegionRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the region repository
     */
    public RegionServiceImpl(RegionRepository repository, SessionManagerProvider smProvider) {
        super(repository, Region.class, smProvider);
        this.repository = repository;
    }

    @Override
    public void findOne(long id, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<Region> findOne = smProvider.withTransactionMaybe( sm -> repository.findByIdAndFetch(sm, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(Region.class))));
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void findAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Region>> findAll = smProvider.withTransactionSingle(repository::findAllAndFetch);
        RxVertxHandler.handleSession(findAll.map(this::mapResultListToJsonArray), resultHandler);
    }

    @Override
    public void findAllByProviderId(long providerId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Region>> findAll = smProvider.withTransactionSingle(sm -> repository
            .findAllByProvider(sm, providerId));
        RxVertxHandler.handleSession(findAll.map(this::mapResultListToJsonArray), resultHandler);
    }

    @Override
    public void findAllByProviderName(String providerName, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Region>> findAll = smProvider.withTransactionSingle(sm -> repository
            .findAllByProvider(sm, providerName));
        RxVertxHandler.handleSession(findAll.map(this::mapResultListToJsonArray), resultHandler);
    }

    @Override
    public void findAllByPlatformId(long platformId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Region>> findAll = smProvider.withTransactionSingle(sm -> repository
            .findAllByPlatformId(sm, platformId));
        RxVertxHandler.handleSession(findAll.map(this::mapResultListToJsonArray), resultHandler);
    }

    // TODO: check user role
    @Override
    public void save(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        Region region = data.mapTo(Region.class);
        Single<Region> create = smProvider.withTransactionSingle(sm -> repository
            .findOneByNameAndProviderId(sm, region.getName(), region.getResourceProvider().getProviderId())
            .flatMapSingle(existingRegion -> Single.<ResourceProvider>error(new AlreadyExistsException(Region.class)))
            .switchIfEmpty(sm.find(ResourceProvider.class, region.getResourceProvider().getProviderId())
            )
            .switchIfEmpty(Single.error(new NotFoundException(ResourceProvider.class)))
            .flatMap(provider -> sm.persist(region))
        );
        RxVertxHandler.handleSession(create.map(JsonObject::mapFrom), resultHandler);
    }
}
