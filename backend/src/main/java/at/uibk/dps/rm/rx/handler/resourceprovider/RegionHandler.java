package at.uibk.dps.rm.rx.handler.resourceprovider;

import at.uibk.dps.rm.rx.handler.ValidationHandler;
import at.uibk.dps.rm.rx.service.rxjava3.database.resourceprovider.RegionService;

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
