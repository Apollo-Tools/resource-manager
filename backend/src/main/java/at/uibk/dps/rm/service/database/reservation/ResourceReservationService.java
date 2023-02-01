package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.repository.reservation.ResourceReservationRepository;
import at.uibk.dps.rm.service.database.ServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

@ProxyGen
@VertxGen
public interface ResourceReservationService extends ServiceInterface {
    @GenIgnore
    static ResourceReservationService create(ResourceReservationRepository resourceReservationRepository) {
        return new ResourceReservationServiceImpl(resourceReservationRepository);
    }

    static ResourceReservationService createProxy(Vertx vertx, String address) {
        return new ResourceReservationServiceVertxEBProxy(vertx, address);
    }

    Future<JsonArray> findAllByReservationId(long id);
}
