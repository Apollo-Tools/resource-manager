package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.repository.log.ReservationLogRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface ReservationLogService extends ServiceInterface {

    @Generated
    @GenIgnore
    static ReservationLogService create(ReservationLogRepository reservationLogRepository) {
        return new ReservationLogServiceImpl(reservationLogRepository);
    }

    @Generated
    static ReservationLogService createProxy(Vertx vertx, String address) {
        return new ReservationLogServiceVertxEBProxy(vertx, address);
    }
}
