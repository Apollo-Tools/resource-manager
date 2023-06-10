package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationStatusService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

/**
 * Implements methods to perform CRUD operations on the resource_deployment_status entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class ResourceDeploymentStatusChecker extends EntityChecker {
    private final ResourceReservationStatusService service;

    /**
     * Create an instance from the service
     *
     * @param service the resource deployment status service
     */
    public ResourceDeploymentStatusChecker(ResourceReservationStatusService service) {
        super(service);
        this.service = service;
    }

    /**
     * Find a resource deployment status by its value.
     *
     * @param value the string value of the status
     * @return a Single that emits the found resource deployment status as JsonObject
     */
    public Single<JsonObject> checkFindOneByStatusValue(String value) {
        Single<JsonObject> findOneByStatusValue = service.findOneByStatusValue(value);
        return ErrorHandler.handleFindOne(findOneByStatusValue);
    }
}
