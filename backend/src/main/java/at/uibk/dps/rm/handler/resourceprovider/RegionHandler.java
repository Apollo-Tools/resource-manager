package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.RegionService;

/**
 * Processes the http requests that concern the region entity.
 *
 * @author matthi-g
 */
public class RegionHandler extends ValidationHandler {
    /**
     * Create an instance from the regionService.
     *
     * @param regionService the service
     */
    public RegionHandler(RegionService regionService) {
        super(regionService);
    }
}
