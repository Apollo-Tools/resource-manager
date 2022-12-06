package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.ResourceProviderService;

public class ResourceProviderHandler extends ValidationHandler {
    public ResourceProviderHandler(ResourceProviderService resourceProviderService) {
        super(new ResourceProviderChecker(resourceProviderService));
    }
}
