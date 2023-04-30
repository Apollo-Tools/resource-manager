package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ResourceReservationStatus;
import at.uibk.dps.rm.repository.reservation.ResourceReservationStatusRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * The interface of the service proxy for the resource_reservation_status entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ResourceReservationStatusService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ResourceReservationStatusService create(ResourceReservationStatusRepository repository) {
        return new ResourceReservationStatusServiceImpl(repository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ResourceReservationStatusService createProxy(Vertx vertx) {
        return new ResourceReservationStatusServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(ResourceReservationStatus.class));
    }

    /**
     * Find a resource reservation status by its value.
     *
     * @param statusValue the value of the status
     * @return a Future that emits the resource reservation status as JsonObject if it exists, else
     * null
     */
    Future<JsonObject> findOneByStatusValue(String statusValue);
}
