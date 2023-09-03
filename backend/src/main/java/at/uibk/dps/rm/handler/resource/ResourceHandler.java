package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.*;

/**
 * Processes the http requests that concern the resource entity.
 *
 * @author matthi-g
 */
@Deprecated
public class ResourceHandler extends ValidationHandler {

    /**
     * Create an instance from the resourceChecker.
     *
     * @param resourceChecker the resource checker
     */
    public ResourceHandler(ResourceChecker resourceChecker) {
        super(resourceChecker);
    }
}
