package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.repository.resource.PlatformRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.util.RxVertxHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
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
    public PlatformServiceImpl(PlatformRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, Platform.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public void findAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Platform>> findAll = withTransactionSingle(repository::findAllAndFetch);
        RxVertxHandler.handleSession(
            findAll.map(platforms -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Platform platform: platforms) {
                    objects.add(JsonObject.mapFrom(platform));
                }
                return new JsonArray(objects);
            }),
            resultHandler
        );
    }
}
