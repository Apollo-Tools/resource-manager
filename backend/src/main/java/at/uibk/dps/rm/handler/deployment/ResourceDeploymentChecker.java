package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.deployment.ResourceDeploymentService;

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
}
