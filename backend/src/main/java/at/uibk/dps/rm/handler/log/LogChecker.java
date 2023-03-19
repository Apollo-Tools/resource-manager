package at.uibk.dps.rm.handler.log;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.log.LogService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

public class LogChecker extends EntityChecker {

    private final LogService logService;

    public LogChecker(LogService logService) {
        super(logService);
        this.logService = logService;
    }

    public Single<JsonArray> checkFindAllByReservationId(long reservationId, long accountId) {
        Single<JsonArray> findAllByReservationId = logService
            .findAllByReservationIdAndAccountId(reservationId, accountId);
        return ErrorHandler.handleFindAll(findAllByReservationId);
    }
}
