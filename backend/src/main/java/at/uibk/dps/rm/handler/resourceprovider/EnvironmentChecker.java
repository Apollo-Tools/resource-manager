package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.EnvironmentService;

/**
 * Implements methods to perform CRUD operations on the environment entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
@Deprecated
public class EnvironmentChecker extends EntityChecker {
    /**
     * Create an instance from the environmentService.
     *
     * @param environmentService the environment service
     */
    public EnvironmentChecker(EnvironmentService environmentService) {
        super(environmentService);
    }

}
