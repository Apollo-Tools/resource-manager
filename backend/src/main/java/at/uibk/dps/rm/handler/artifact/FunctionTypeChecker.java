package at.uibk.dps.rm.handler.artifact;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.artifact.FunctionTypeService;

/**
 * Implements methods to perform CRUD operations on the function artifact type entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class FunctionTypeChecker extends EntityChecker {
    /**
     * Create an instance from the service.
     *
     * @param service the service type service
     */
    public FunctionTypeChecker(FunctionTypeService service) {
        super(service);
    }
}
