package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ServiceReservationService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

/**
 * Implements methods to perform CRUD operations on the service_deployment entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class ServiceDeploymentChecker extends EntityChecker {

    private final ServiceReservationService service;

    /**
     * Create an instance from the service.
     *
     * @param service the service deployment service
     */
    public ServiceDeploymentChecker(ServiceReservationService service) {
        super(service);
        this.service = service;
    }

    /**
     * Find all service deployments by deployment.
     *
     * @param id the id of the deployment
     * @return a Single that emits all found service deployments as JsonArray
     */
    public Single<JsonArray> checkFindAllByDeploymentId(long id) {
        final Single<JsonArray> findAllByDeploymentId = service.findAllByReservationId(id);
        return ErrorHandler.handleFindAll(findAllByDeploymentId);
    }

    /**
     * Check whether a service deployment is ready for startup
     *
     * @param deploymentId the id of the deployment
     * @param resourceDeploymentId the id of the resource deployment
     * @param accountId the id of the creator of the deployment
     * @return a Single that emits true if the service deployment is ready, else false
     */
    public Completable checkReadyForStartup(long deploymentId,
        long resourceDeploymentId, long accountId) {
        Single<Boolean> exists = service.existsReadyForContainerStartupAndTermination(deploymentId,
            resourceDeploymentId,
          accountId);
        return ErrorHandler.handleExistsOne(exists).ignoreElement();
    }
}
