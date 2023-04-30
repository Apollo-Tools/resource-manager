package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.model.ResourceReservation;
import at.uibk.dps.rm.repository.reservation.ResourceReservationRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

/**
 * The interface of the service proxy for the resource_reservation entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ResourceReservationService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ResourceReservationService create(ResourceReservationRepository resourceReservationRepository) {
        return new ResourceReservationServiceImpl(resourceReservationRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ResourceReservationService createProxy(Vertx vertx) {
        return new ResourceReservationServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(ResourceReservation.class));
    }

    /**
     * Find all resource reservations that belong to a reservation.
     *
     * @param reservationId the reservationId of the reservation
     * @return a Future that emits all resource reservations as JsonArray
     */
    Future<JsonArray> findAllByReservationId(long reservationId);

    /**
     * Update the trigger url of a resource reservation by its function resource and reservation.
     *
     * @param functionResourceId the id of the function resource
     * @param reservationId the id of  the reservation
     * @param triggerUrl the new trigger url
     * @return an empty Future
     */
    Future<Void> updateTriggerUrl(long functionResourceId, long reservationId, String triggerUrl);

    /**
     * Update the status of all resource reservations by their reservation.
     *
     * @param reservationId the id of the reservation
     * @param reservationStatusValue the new status
     * @return an empty Future
     */
    Future<Void> updateSetStatusByReservationId(long reservationId, ReservationStatusValue reservationStatusValue);
}
