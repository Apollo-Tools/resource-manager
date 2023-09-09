package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.repository.log.LogRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the implementation of the {@link LogService}.
 *
 * @author matthi-g
 */
public class LogServiceImpl extends DatabaseServiceProxy<Log> implements LogService {

    private final LogRepository repository;

    /**
     * Create an instance from the logRepository.
     *
     * @param repository the log repository
     */
    public LogServiceImpl(LogRepository repository, SessionManagerProvider smProvider) {
        super(repository, Log.class, smProvider);
        this.repository = repository;
    }

    @Override
    public void findAllByDeploymentIdAndAccountId(long deploymentId, long accountId,
            Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Log>> findAll = smProvider.withTransactionSingle(sm -> repository
            .findAllByDeploymentIdAndAccountId(sm, deploymentId, accountId));
        RxVertxHandler.handleSession(
            findAll.map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Log entity: result) {
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            }),
            resultHandler
        );
    }
}
