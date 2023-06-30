package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.ValidationHandler;

/**
 * Processes the http requests that concern the region entity.
 *
 * @author matthi-g
 */
public class RegionHandler extends ValidationHandler {
    /**
     * Create an instance from the regionChecker and providerChecker.
     *
     * @param regionChecker the region checker
     */
    public RegionHandler(RegionChecker regionChecker) {
        super(regionChecker);
    }
}
