package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.repository.deployment.DeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * The interface of the service proxy for the reservation entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ReservationService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ReservationService create(DeploymentRepository reservationRepository) {
        return new ReservationServiceImpl(reservationRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ReservationService createProxy(Vertx vertx) {
        return new ReservationServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Deployment.class));
    }

    /**
     * Find all reservations by their creator account.
     *
     * @param accountId the id of the account
     * @return a Future that emits all reservations as JsonArray
     */
    Future<JsonArray> findAllByAccountId(long accountId);

    /**
     * Find one reservation by its id and creator account.
     *
     * @param id the id of the reservation
     * @param accountId the id of the creator account
     * @return a Future that emits the reservation as JsonObject if it exists, else null
     */
    Future<JsonObject> findOneByIdAndAccountId(long id, long accountId);
}
