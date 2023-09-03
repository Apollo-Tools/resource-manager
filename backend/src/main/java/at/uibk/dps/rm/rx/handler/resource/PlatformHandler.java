package at.uibk.dps.rm.rx.handler.resource;

import at.uibk.dps.rm.rx.handler.ValidationHandler;
import at.uibk.dps.rm.rx.service.rxjava3.database.resource.PlatformService;

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
