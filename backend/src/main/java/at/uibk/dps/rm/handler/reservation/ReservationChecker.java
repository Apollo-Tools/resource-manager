package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ReservationService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Implements methods to perform CRUD operations on the reservation entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class ReservationChecker  extends EntityChecker {

    private final ReservationService reservationService;

    /**
     * Create an instance from the reservationService.
     *
     * @param reservationService the reservation service
     */
    public ReservationChecker(final ReservationService reservationService) {
        super(reservationService);
        this.reservationService = reservationService;

    }

    /**
     * Find all reservations by account.
     *
     * @return a Single that emits all found reservations as JsonArray
     */
    public Single<JsonArray> checkFindAll(long accountId) {
        return ErrorHandler.handleFindAll(reservationService.findAllByAccountId(accountId));
    }

    /**
     * Find a reservation by its id and account.
     *
     * @param id the id of the reservation
     * @param accountId  the id of the account
     * @return a Single that emits the found reservation as JsonObject
     */
    public Single<JsonObject> checkFindOne(long id, long accountId) {
        Single<JsonObject> findOneById = reservationService.findOneByIdAndAccountId(id, accountId);
        return ErrorHandler.handleFindOne(findOneById);
    }

    /**
     * Submit the creation of a new entity.
     *
     * @param accountId id of the account
     * @return a Single that emits the persisted entity
     */
    public Single<JsonObject> submitCreateReservation(long accountId) {
        Reservation reservation = new Reservation();
        reservation.setIsActive(true);
        Account account = new Account();
        account.setAccountId(accountId);
        reservation.setCreatedBy(account);
        return this.submitCreate(JsonObject.mapFrom(reservation));
    }
}
