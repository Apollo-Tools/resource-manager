package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.FunctionReservation;
import at.uibk.dps.rm.repository.reservation.FunctionReservationRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface FunctionReservationService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static FunctionReservationService create(FunctionReservationRepository repository) {
        return new FunctionReservationServiceImpl(repository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static FunctionReservationService createProxy(Vertx vertx) {
        return new FunctionReservationServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(FunctionReservation.class));
    }
}
