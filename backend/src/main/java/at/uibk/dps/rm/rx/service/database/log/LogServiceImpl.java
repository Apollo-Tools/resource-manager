package at.uibk.dps.rm.rx.service.database.log;

import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.rx.repository.log.LogRepository;
import at.uibk.dps.rm.rx.service.database.DatabaseServiceProxy;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

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
    public LogServiceImpl(LogRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, Log.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public void findAllByDeploymentIdAndAccountId(long deploymentId, long accountId,
            Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Log>> findAll = withTransactionSingle(sessionManager -> repository
            .findAllByDeploymentIdAndAccountId(sessionManager, deploymentId, accountId));
        handleSession(
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
