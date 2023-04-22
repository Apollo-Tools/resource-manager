package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.model.ResourceReservation;
import at.uibk.dps.rm.repository.reservation.ResourceReservationRepository;
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
public interface ResourceReservationService extends DatabaseServiceInterface {

    @Generated
    @GenIgnore
    static ResourceReservationService create(ResourceReservationRepository resourceReservationRepository) {
        return new ResourceReservationServiceImpl(resourceReservationRepository);
    }

    @Generated
    static ResourceReservationService createProxy(Vertx vertx) {
        return new ResourceReservationServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(ResourceReservation.class));
    }

    Future<JsonArray> findAllByReservationId(long id);

    Future<Void> updateTriggerUrl(long functionResourceId, long reservationId, String triggerUrl);

    Future<Void> updateSetStatusByReservationId(long reservationId, ReservationStatusValue reservationStatusValue);
}
