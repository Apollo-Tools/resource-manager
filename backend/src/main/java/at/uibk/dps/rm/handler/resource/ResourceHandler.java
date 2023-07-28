package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.*;

/**
 * Processes the http requests that concern the resource entity.
 *
 * @author matthi-g
 */
public class ResourceHandler extends ValidationHandler {

    /**
     * Create an instance from the resourceChecker.
     *
     * @param resourceChecker the resource checker
     */
    public ResourceHandler(ResourceChecker resourceChecker) {
        super(resourceChecker);
    }

    // TODO: delete metric values on delete check if resource has metric values
}
