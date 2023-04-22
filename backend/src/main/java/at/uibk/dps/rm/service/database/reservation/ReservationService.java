package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.repository.reservation.ReservationRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface ReservationService extends DatabaseServiceInterface {

    @Generated
    @GenIgnore
    static ReservationService create(ReservationRepository reservationRepository) {
        return new ReservationServiceImpl(reservationRepository);
    }

    @Generated
    static ReservationService createProxy(Vertx vertx) {
        return new ReservationServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Reservation.class));
    }

    Future<JsonArray> findAllByAccountId(long accountId);

    Future<JsonObject> findOneByIdAndAccountId(long id, long accountId);
}
