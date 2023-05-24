package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.repository.resource.ResourceTypeRepository;
import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;

/**
 * This is the implementation of the #ResourceTypeService.
 *
 * @author matthi-g
 */
public class ResourceTypeServiceImpl extends DatabaseServiceProxy<ResourceType> implements ResourceTypeService  {

    /**
     * Create an instance from the resourceTypeRepository.
     *
     * @param resourceTypeRepository the resource type repository
     */
    public ResourceTypeServiceImpl(ResourceTypeRepository resourceTypeRepository) {
        super(resourceTypeRepository, ResourceType.class);
    }
}
