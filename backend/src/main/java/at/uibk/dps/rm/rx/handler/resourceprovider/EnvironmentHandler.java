package at.uibk.dps.rm.rx.handler.resourceprovider;

import at.uibk.dps.rm.rx.handler.ValidationHandler;
import at.uibk.dps.rm.rx.service.rxjava3.database.resourceprovider.EnvironmentService;

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
