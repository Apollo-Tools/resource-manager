package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.entity.model.ResourceDeployment;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

/**
 * This is the implementation of the #ResourceReservationService.
 *
 * @author matthi-g
 */
public class ResourceReservationServiceImpl extends DatabaseServiceProxy<ResourceDeployment> implements ResourceReservationService {

    private final ResourceDeploymentRepository resourceReservationRepository;

    /**
     * Create aninstance from the resourceReservationRepositry.
     *
     * @param resourceReservationRepository the resource reservation repository
     */
    public ResourceReservationServiceImpl(ResourceDeploymentRepository resourceReservationRepository) {
        super(resourceReservationRepository, ResourceDeployment.class);
        this.resourceReservationRepository = resourceReservationRepository;
    }

    @Override
    public Future<JsonArray> findAllByReservationId(long reservationId) {
        return Future
                .fromCompletionStage(resourceReservationRepository.findAllByDeploymentId(reservationId))
                .map(result -> {
                    ArrayList<JsonObject> objects = new ArrayList<>();
                    for (ResourceDeployment entity: result) {
                        // TODO: fix
                        if (entity instanceof ServiceDeployment) {
                            ((ServiceDeployment) entity).setService(null);
                        } else if (entity instanceof FunctionDeployment) {
                            ((FunctionDeployment) entity).setFunction(null);
                        }
                        entity.setDeployment(null);
                        entity.setResource(null);
                        objects.add(JsonObject.mapFrom(entity));
                    }
                    return new JsonArray(objects);
                });
    }

    @Override
    public Future<Void> updateTriggerUrl(long id, String triggerUrl) {
        return Future
            .fromCompletionStage(resourceReservationRepository.updateTriggerUrl(id, triggerUrl))
            .mapEmpty();
    }

    @Override
    public Future<Void> updateSetStatusByReservationId(long reservationId, DeploymentStatusValue reservationStatusValue) {
        return Future
            .fromCompletionStage(resourceReservationRepository
                .updateDeploymentStatusByDeploymentId(reservationId, reservationStatusValue))
            .mapEmpty();
    }
}
