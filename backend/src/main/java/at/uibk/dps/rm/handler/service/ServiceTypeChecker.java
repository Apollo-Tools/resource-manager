package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.service.ServiceTypeService;

/**
 * Implements methods to perform CRUD operations on the service_type entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class ServiceTypeChecker extends EntityChecker {
    /**
     * Create an instance from the service.
     *
     * @param service the service type service
     */
    public ServiceTypeChecker(ServiceTypeService service) {
        super(service);
    }
}
