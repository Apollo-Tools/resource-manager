package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.handler.ValidationHandler;

/**
 * Processes the http requests that concern the service_type entity.
 *
 * @author matthi-g
 */
@Deprecated
public class K8sServiceTypeHandler extends ValidationHandler {
    /**
     * Create an instance from the serviceTypeChecker.
     *
     * @param serviceTypeChecker the service type checker
     */
    public K8sServiceTypeHandler(K8sServiceTypeChecker serviceTypeChecker) {
        super(serviceTypeChecker);
    }
}
