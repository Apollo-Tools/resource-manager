package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.ResourceProviderService;

/**
 * Implements methods to perform CRUD operations on the resource_provider entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
@Deprecated
public class ResourceProviderChecker extends EntityChecker {

    /**
     * Create an instance from the resourceProviderService.
     *
     * @param resourceProviderService the resource provider service
     */
    public ResourceProviderChecker(ResourceProviderService resourceProviderService) {
        super(resourceProviderService);
    }
}
