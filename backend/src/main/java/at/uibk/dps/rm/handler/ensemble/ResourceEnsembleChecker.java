package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.ResourceEnsembleService;

public class ResourceEnsembleChecker extends EntityChecker {

    private final ResourceEnsembleService service;
    /**
     * Create an instance from the service.
     *
     * @param service the resource ensemble service to use
     */
    public ResourceEnsembleChecker(ResourceEnsembleService service) {
        super(service);
        this.service = service;
    }
}
