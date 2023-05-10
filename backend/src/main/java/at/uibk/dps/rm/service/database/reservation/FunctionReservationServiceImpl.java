package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.model.FunctionReservation;
import at.uibk.dps.rm.repository.reservation.FunctionReservationRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

public class FunctionReservationServiceImpl  extends DatabaseServiceProxy<FunctionReservation> implements
    FunctionReservationService {

    private final FunctionReservationRepository repository;

    public FunctionReservationServiceImpl(FunctionReservationRepository repository) {
        super(repository, FunctionReservation.class);
        this.repository = repository;
    }

    @Override
    public Future<JsonArray> findAllByReservationId(long reservationId) {
        return Future
            .fromCompletionStage(repository.findAllByReservationId(reservationId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (FunctionReservation entity: result) {
                    entity.setReservation(null);
                    entity.setStatus(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }
}
