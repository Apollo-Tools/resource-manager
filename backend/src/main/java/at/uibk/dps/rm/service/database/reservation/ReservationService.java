package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.repository.reservation.ReservationRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface ReservationService extends ServiceInterface {

    @Generated
    @GenIgnore
    static ReservationService create(ReservationRepository reservationRepository) {
        return new ReservationServiceImpl(reservationRepository);
    }

    @Generated
    static ReservationService createProxy(Vertx vertx, String address) {
        return new ReservationServiceVertxEBProxy(vertx, address);
    }

    Future<JsonArray> findAllByAccountId(long accountId);

    Future<JsonObject> findOneByIdAndAccountId(long id, long accountId);
}
