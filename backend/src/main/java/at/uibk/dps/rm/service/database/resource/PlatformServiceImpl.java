package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.repository.resource.PlatformRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

import java.util.List;

/**
 * This is the implementation of the {@link PlatformService}.
 *
 * @author matthi-g
 */
public class PlatformServiceImpl extends DatabaseServiceProxy<Platform> implements PlatformService {

    private final PlatformRepository repository;
    /**
     * Create an instance from the platformRepository.
     *
     * @param repository the platform repository
     */
    public PlatformServiceImpl(PlatformRepository repository, SessionManagerProvider smProvider) {
        super(repository, Platform.class, smProvider);
        this.repository = repository;
    }

    @Override
    public void findAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Platform>> findAll = smProvider.withTransactionSingle(repository::findAllAndFetch);
        RxVertxHandler.handleSession(findAll.map(this::mapResultListToJsonArray), resultHandler);
    }

    @Override
    public void findAllByResourceProvider(String resourceProvider, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Platform>> findAll = smProvider.withTransactionSingle(sessionManager -> repository
            .findAllByResourceProvider(sessionManager, resourceProvider));
        RxVertxHandler.handleSession(findAll.map(this::mapResultListToJsonArray), resultHandler);
    }
}
