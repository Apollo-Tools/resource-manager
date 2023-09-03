package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.ValidationHandler;

/**
 * Processes the http requests that concern the resource_type entity.
 *
 * @author matthi-g
 */
@Deprecated
public class ResourceTypeHandler extends ValidationHandler {

    /**
     * Create an instance from the resourceTypeChecker and resourceChecker.
     *
     * @param resourceTypeChecker the resource type checker
     */
    public ResourceTypeHandler(ResourceTypeChecker resourceTypeChecker) {
        super(resourceTypeChecker);
    }
}
