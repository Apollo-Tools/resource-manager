package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.deployment.ServiceDeploymentService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Implements methods to perform CRUD operations on the service_deployment entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
@Deprecated
public class ServiceDeploymentChecker extends EntityChecker {

    private final ServiceDeploymentService service;

    /**
     * Create an instance from the service.
     *
     * @param service the service deployment service
     */
    public ServiceDeploymentChecker(ServiceDeploymentService service) {
        super(service);
        this.service = service;
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
