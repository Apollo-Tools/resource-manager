package at.uibk.dps.rm.rx.handler.service;

import at.uibk.dps.rm.rx.handler.ValidationHandler;
import at.uibk.dps.rm.rx.service.rxjava3.database.service.K8sServiceTypeService;

/**
 * Processes the http requests that concern the service_type entity.
 *
 * @author matthi-g
 */
public class K8sServiceTypeHandler extends ValidationHandler {
    /**
     * Create an instance from the service.
     *
     * @param service the service
     */
    public K8sServiceTypeHandler(K8sServiceTypeService service) {
        super(service);
    }
}
