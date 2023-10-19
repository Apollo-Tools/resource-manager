package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resource.PlatformService;

/**
 * Processes the http requests that concern the platform entity.
 *
 * @author matthi-g
 */
public class PlatformHandler extends ValidationHandler {
    /**
     * Create an instance from the platformService.
     *
     * @param platformService the service
     */
    public PlatformHandler(PlatformService platformService) {
        super(platformService);
    }
}
