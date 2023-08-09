package at.uibk.dps.rm.handler.artifact;

import at.uibk.dps.rm.handler.ValidationHandler;

/**
 * Processes the http requests that concern the service artifact type entity.
 *
 * @author matthi-g
 */
public class ServiceTypeHandler extends ValidationHandler {
    /**
     * Create an instance from the serviceTypeChecker.
     *
     * @param serviceTypeChecker the artifact type checker
     */
    public ServiceTypeHandler(ServiceTypeChecker serviceTypeChecker) {
        super(serviceTypeChecker);
    }
}
