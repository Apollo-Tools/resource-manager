package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.repository.log.LogRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #LogService.
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
    public Future<JsonArray> findAllByDeploymentIdAndAccountId(long deploymentId, long accountId) {
        CompletionStage<List<Log>> findAll = withSession(session ->
            repository.findAllByDeploymentIdAndAccountId(session, deploymentId, accountId));
        return Future.fromCompletionStage(findAll)
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Log entity: result) {
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }
}
