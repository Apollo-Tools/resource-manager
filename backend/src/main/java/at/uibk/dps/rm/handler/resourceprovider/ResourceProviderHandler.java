package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.ValidationHandler;

/**
 * Processes the http requests that concern the resource_provider entity.
 *
 * @author matthi-g
 */
public class ResourceProviderHandler extends ValidationHandler {

    /**
     * Create an instance from the providerChecker.
     *
     * @param providerChecker the resource provider checker
     */
    public ResourceProviderHandler(ResourceProviderChecker providerChecker) {
        super(providerChecker);
    }
}
