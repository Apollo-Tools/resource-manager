package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.deployment.ResourceDeploymentService;
import io.reactivex.rxjava3.core.Completable;

/**
 * Implements methods to perform CRUD operations on the resource_deployment entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
@Deprecated
public class ResourceDeploymentChecker extends EntityChecker {

    private final ResourceDeploymentService resourceDeploymentService;

    /**
     * Create an instance from the resourceDeploymentService.
     *
     * @param resourceDeploymentService the resource deployment service
     */
    public ResourceDeploymentChecker(ResourceDeploymentService resourceDeploymentService) {
        super(resourceDeploymentService);
        this.resourceDeploymentService = resourceDeploymentService;
    }

    /**
     * Submit the update of the status of a resource deployment.
     *
     * @param deploymentId the id of the deployment
     * @param statusValue the new status
     * @return a Completable
     */
    public Completable submitUpdateStatus(long deploymentId, DeploymentStatusValue statusValue) {
        return resourceDeploymentService.updateSetStatusByDeploymentId(deploymentId, statusValue);
    }
}
