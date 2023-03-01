package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.repository.reservation.ResourceReservationStatusRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface ResourceReservationStatusService extends ServiceInterface {
    @GenIgnore
    static ResourceReservationStatusService create(ResourceReservationStatusRepository repository) {
        return new ResourceReservationStatusServiceImpl(repository);
    }

    static ResourceReservationStatusService createProxy(Vertx vertx, String address) {
        return new ResourceReservationStatusServiceVertxEBProxy(vertx, address);
    }

    Future<JsonObject> findOneByStatusValue(String statusValue);
}
