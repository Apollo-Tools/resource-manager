package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.repository.deployment.FunctionDeploymentRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

/**
 * The interface of the service proxy for the function_reservation entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface FunctionReservationService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static FunctionReservationService create(FunctionDeploymentRepository repository) {
        return new FunctionReservationServiceImpl(repository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static FunctionReservationService createProxy(Vertx vertx) {
        return new FunctionReservationServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(FunctionDeployment.class));
    }

    /**
     * Find all function reservations that belong to a reservation.
     *
     * @param reservationId the reservationId of the reservation
     * @return a Future that emits all function reservations as JsonArray
     */
    Future<JsonArray> findAllByReservationId(long reservationId);
}
