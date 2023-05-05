package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.handler.ValidationHandler;

public class ServiceHandler extends ValidationHandler {
    /**
     * Create an instance from the serviceChecker.
     *
     * @param serviceChecker the service checker
     */
    public ServiceHandler(ServiceChecker serviceChecker) {
        super(serviceChecker);
    }
}
