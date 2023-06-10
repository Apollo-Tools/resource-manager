package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.DeploymentLog;
import at.uibk.dps.rm.repository.log.DeploymentLogRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

/**
 * The interface of the service proxy for the reservation_log entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ReservationLogService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ReservationLogService create(DeploymentLogRepository reservationLogRepository) {
        return new ReservationLogServiceImpl(reservationLogRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ReservationLogService createProxy(Vertx vertx) {
        return new ReservationLogServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(DeploymentLog.class));
    }
}
