package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ReservationLog;
import at.uibk.dps.rm.repository.log.ReservationLogRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface ReservationLogService extends DatabaseServiceInterface {

    @Generated
    @GenIgnore
    static ReservationLogService create(ReservationLogRepository reservationLogRepository) {
        return new ReservationLogServiceImpl(reservationLogRepository);
    }

    @Generated
    static ReservationLogService createProxy(Vertx vertx) {
        return new ReservationLogServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(ReservationLog.class));
    }
}
