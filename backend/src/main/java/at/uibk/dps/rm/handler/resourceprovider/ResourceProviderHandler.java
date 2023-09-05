package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.ResourceProviderService;

/**
 * Processes the http requests that concern the resource_provider entity.
 *
 * @author matthi-g
 */
public class ResourceProviderHandler extends ValidationHandler {

    /**
     * Create an instance from the providerService.
     *
     * @param providerService the service
     */
    public ResourceProviderHandler(ResourceProviderService providerService) {
        super(providerService);
    }
}
