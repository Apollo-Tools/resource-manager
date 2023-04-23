package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.repository.reservation.ReservationRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

/**
 * This is the implementation of the #ReservationService.
 *
 * @author matthi-g
 */
public class ReservationServiceImpl extends DatabaseServiceProxy<Reservation> implements ReservationService {

    private final ReservationRepository reservationRepository;

    /**
     * Create an instance from the reservationRepository.
     *
     * @param reservationRepository the reservation repository
     */
    public ReservationServiceImpl(ReservationRepository reservationRepository) {
        super(reservationRepository, Reservation.class);
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Future<JsonArray> findAllByAccountId(long accountId) {
        return Future
            .fromCompletionStage(reservationRepository.findAllByAccountId(accountId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Reservation entity: result) {
                    entity.setCreatedBy(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<JsonObject> findOneByIdAndAccountId(long id, long accountId) {
        return Future
            .fromCompletionStage(reservationRepository.findByIdAndAccountId(id, accountId))
            .map(result -> {
                if (result != null) {
                    result.setCreatedBy(null);
                }
                return JsonObject.mapFrom(result);
            });
    }
}
