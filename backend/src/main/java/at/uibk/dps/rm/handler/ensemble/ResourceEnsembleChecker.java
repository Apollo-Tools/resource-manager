package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.ResourceEnsembleService;

/**
 * Implements methods to perform CRUD operations on the resource_ensemble entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class ResourceEnsembleChecker extends EntityChecker {
    /**
     * Create an instance from the service.
     *
     * @param service the resource ensemble service to use
     */
    public ResourceEnsembleChecker(ResourceEnsembleService service) {
        super(service);
    }
}
