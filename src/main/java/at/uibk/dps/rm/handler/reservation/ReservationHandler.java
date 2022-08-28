package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ReservationService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ReservationHandler extends ValidationHandler {

    public ReservationHandler(ReservationService reservationService) {
        super(new ReservationChecker(reservationService));
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        System.out.println(requestBody.encodePrettily());
        return Single.just(requestBody);
    }
}
