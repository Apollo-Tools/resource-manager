package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.repository.deployment.FunctionDeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

/**
 * This is the implementation of the #FunctionReservationService.
 *
 * @author matthi-g
 */
public class FunctionReservationServiceImpl  extends DatabaseServiceProxy<FunctionDeployment> implements
    FunctionReservationService {

    private final FunctionDeploymentRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the function reservation repository
     */
    public FunctionReservationServiceImpl(FunctionDeploymentRepository repository) {
        super(repository, FunctionDeployment.class);
        this.repository = repository;
    }

    @Override
    public Future<JsonArray> findAllByReservationId(long reservationId) {
        return Future
            .fromCompletionStage(repository.findAllByDeploymentId(reservationId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (FunctionDeployment entity: result) {
                    entity.setDeployment(null);
                    entity.getResource().getRegion().getResourceProvider().setProviderPlatforms(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }
}
