package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.ValidationHandler;

/**
 * Processes the http requests that concern the platform entity.
 *
 * @author matthi-g
 */
@Deprecated
public class PlatformHandler extends ValidationHandler {
    /**
     * Create an instance from the platformChecker.
     *
     * @param platformChecker the platform checker
     */
    public PlatformHandler(PlatformChecker platformChecker) {
        super(platformChecker);
    }
}
