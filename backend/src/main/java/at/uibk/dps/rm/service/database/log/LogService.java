package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.repository.log.LogRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

@ProxyGen
@VertxGen
public interface LogService extends ServiceInterface {
    @GenIgnore
    static LogService create(LogRepository logRepository) {
        return new LogServiceImpl(logRepository);
    }

    static LogService createProxy(Vertx vertx, String address) {
        return new LogServiceVertxEBProxy(vertx, address);
    }

    Future<JsonArray> findAllByReservationId(long reservationId);
}
