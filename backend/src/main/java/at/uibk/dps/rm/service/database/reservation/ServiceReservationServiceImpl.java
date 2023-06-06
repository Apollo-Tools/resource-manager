package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.model.ServiceReservation;
import at.uibk.dps.rm.repository.reservation.ServiceReservationRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Objects;

/**
 * This is the implementation of the #ServiceReservationService.
 *
 * @author matthi-g
 */
public class ServiceReservationServiceImpl  extends DatabaseServiceProxy<ServiceReservation>
    implements ServiceReservationService {

    private final ServiceReservationRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the service reservation repository
     */
    public ServiceReservationServiceImpl(ServiceReservationRepository repository) {
        super(repository, ServiceReservation.class);
        this.repository = repository;
    }

    @Override
    public Future<JsonArray> findAllByReservationId(long reservationId) {
        return Future
            .fromCompletionStage(repository.findAllByReservationId(reservationId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (ServiceReservation entity: result) {
                    entity.getResource().getRegion().getResourceProvider().setProviderPlatforms(null);
                    entity.setReservation(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Boolean> existsReadyForContainerStartupAndTermination(long reservationId,
        long resourceReservationId, long accountId) {
        return Future.fromCompletionStage(repository.findOneByReservationStatus(reservationId, resourceReservationId,
                accountId, ReservationStatusValue.DEPLOYED))
            .map(Objects::nonNull);
    }
}
