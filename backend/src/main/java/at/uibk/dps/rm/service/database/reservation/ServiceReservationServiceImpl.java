package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.repository.deployment.ServiceDeploymentRepository;
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
public class ServiceReservationServiceImpl  extends DatabaseServiceProxy<ServiceDeployment>
    implements ServiceReservationService {

    private final ServiceDeploymentRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the service reservation repository
     */
    public ServiceReservationServiceImpl(ServiceDeploymentRepository repository) {
        super(repository, ServiceDeployment.class);
        this.repository = repository;
    }

    @Override
    public Future<JsonArray> findAllByReservationId(long reservationId) {
        return Future
            .fromCompletionStage(repository.findAllByDeploymentId(reservationId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (ServiceDeployment entity: result) {
                    entity.getResource().getRegion().getResourceProvider().setProviderPlatforms(null);
                    entity.setDeployment(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Boolean> existsReadyForContainerStartupAndTermination(long reservationId,
        long resourceReservationId, long accountId) {
        return Future.fromCompletionStage(repository.findOneByDeploymentStatus(reservationId, resourceReservationId,
                accountId, DeploymentStatusValue.DEPLOYED))
            .map(Objects::nonNull);
    }
}
