package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.EnvironmentService;

/**
 * Processes the http requests that concern the environment entity.
 *
 * @author matthi-g
 */
public class EnvironmentHandler extends ValidationHandler {
    /**
     * Create an instance from the environmentService.
     *
     * @param environmentService the service
     */
    public EnvironmentHandler(EnvironmentService environmentService) {
        super(environmentService);
    }
}
