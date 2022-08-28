package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.repository.ReservationRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface ReservationService extends ServiceInterface {
    @GenIgnore
    static ReservationService create(ReservationRepository reservationRepository) {
        return new ReservationServiceImpl(reservationRepository);
    }

    static ReservationService createProxy(Vertx vertx, String address) {
        return new ReservationServiceVertxEBProxy(vertx, address);
    }
}
