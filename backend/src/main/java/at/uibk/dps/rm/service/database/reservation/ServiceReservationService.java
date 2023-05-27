package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ServiceReservation;
import at.uibk.dps.rm.repository.reservation.ServiceReservationRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

/**
 * The interface of the service proxy for the service_reservation entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ServiceReservationService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ServiceReservationService create(ServiceReservationRepository repository) {
        return new ServiceReservationServiceImpl(repository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ServiceReservationService createProxy(Vertx vertx) {
        return new ServiceReservationServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(ServiceReservation.class));
    }

    /**
     * Find all service reservations that belong to a reservation.
     *
     * @param reservationId the reservationId of the reservation
     * @return a Future that emits all service reservations as JsonArray
     */
    Future<JsonArray> findAllByReservationId(long reservationId);

    /**
     * Check if a service reservation is ready for startup or termination
     *
     * @param reservationId the id of the reservation
     * @param resourceReservationId the id of the resource reservation
     * @param accountId the account id of the creator
     * @return a Future that emits true if the function exists, else false
     */
    Future<Boolean> existsReadyForContainerStartupAndTermination(long reservationId,
        long resourceReservationId, long accountId);
}
