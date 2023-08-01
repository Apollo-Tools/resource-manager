package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.service.ServiceService;

/**
 * Implements methods to perform CRUD operations on the service entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class ServiceChecker extends EntityChecker {
    /**
     * Create an instance from the service service.
     *
     * @param service the service to use
     */
    public ServiceChecker(ServiceService service) {
        super(service);
    }
}
