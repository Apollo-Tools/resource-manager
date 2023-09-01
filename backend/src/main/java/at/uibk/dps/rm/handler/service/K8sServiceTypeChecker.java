package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.service.K8sServiceTypeService;

/**
 * Implements methods to perform CRUD operations on the service_type entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
@Deprecated
public class K8sServiceTypeChecker extends EntityChecker {
    /**
     * Create an instance from the service.
     *
     * @param service the service type service
     */
    public K8sServiceTypeChecker(K8sServiceTypeService service) {
        super(service);
    }
}
