package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.repository.log.LogRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

public class LogServiceImpl extends ServiceProxy<Log> implements LogService {

    private final LogRepository logRepository;

    public LogServiceImpl(LogRepository logRepository) {
        super(logRepository, Log.class);
        this.logRepository = logRepository;
    }

    @Override
    public Future<JsonArray> findAllByReservationIdAndAccountId(long reservationId, long accountId) {
        return Future
            .fromCompletionStage(logRepository.findAllByReservationIdAndAccountId(reservationId, accountId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Log entity: result) {
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }
}
