package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.function.RuntimeService;

/**
 * Implements methods to perform CRUD operations on the runtime entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
@Deprecated
public class RuntimeChecker extends EntityChecker {

    /**
     * Create an instance from the RuntimeService.
     *
     * @param runtimeService the runtime service
     */
    public RuntimeChecker(RuntimeService runtimeService) {
        super(runtimeService);
    }
}
