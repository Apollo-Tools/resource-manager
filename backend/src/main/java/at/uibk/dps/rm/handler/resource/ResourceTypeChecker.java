package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceTypeService;
/**
 * Implements methods to perform CRUD operations on the resource_type entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class ResourceTypeChecker extends EntityChecker {

    /**
     * Create an instance from the resourceTypeService.
     *
     * @param resourceTypeService the resource type service
     */
    public ResourceTypeChecker(ResourceTypeService resourceTypeService) {
        super(resourceTypeService);
    }
}
