package at.uibk.dps.rm.handler.log;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.log.LogService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

/**
 * Implements methods to perform CRUD operations on the log entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class LogChecker extends EntityChecker {

    private final LogService logService;

    /**
     * Create an instance from the logService.
     *
     * @param logService the log service
     */
    public LogChecker(final LogService logService) {
        super(logService);
        this.logService = logService;
    }

    /**
     * Find all logs by reservation and account.
     *
     * @param reservationId the id of the reservation
     * @param accountId the id of the account
     * @return a Single that emits all found logs as JsonArray
     */
    public Single<JsonArray> checkFindAllByReservationId(long reservationId, long accountId) {
        Single<JsonArray> findAllByReservationId = logService
            .findAllByReservationIdAndAccountId(reservationId, accountId);
        return ErrorHandler.handleFindAll(findAllByReservationId);
    }
}
