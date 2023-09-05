package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resource.PlatformService;

/**
 * Processes the http requests that concern the regions linked to the platform entity.
 *
 * @author matthi-g
 */
@Deprecated
public class PlatformRegionHandler extends ValidationHandler {

    /**
     * Create an instance from the platformService.
     *
     * @param platformService the platform checker
     */
    public PlatformRegionHandler(PlatformService platformService) {
        super(platformService);
    }
}
