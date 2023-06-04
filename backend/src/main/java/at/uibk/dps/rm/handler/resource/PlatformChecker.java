package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.resource.PlatformService;

/**
 * Implements methods to perform CRUD operations on the platform entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class PlatformChecker extends EntityChecker {

    /**
     * Create an instance from the platformService.
     *
     * @param platformService the platform service
     */
    public PlatformChecker(PlatformService platformService) {
        super(platformService);
    }
}
