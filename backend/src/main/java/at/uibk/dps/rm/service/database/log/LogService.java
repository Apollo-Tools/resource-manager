package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.repository.log.LogRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

@ProxyGen
@VertxGen
public interface LogService extends DatabaseServiceInterface {

    @Generated
    @GenIgnore
    static LogService create(LogRepository logRepository) {
        return new LogServiceImpl(logRepository);
    }

    @Generated
    static LogService createProxy(Vertx vertx) {
        return new LogServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Log.class));
    }

    Future<JsonArray> findAllByReservationIdAndAccountId(long reservationId, long accountId);
}
