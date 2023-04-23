package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.repository.resource.ResourceTypeRepository;
import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;

import java.util.Objects;

/**
 * This is the implementation of the #ResourceTypeService.
 *
 * @author matthi-g
 */
public class ResourceTypeServiceImpl extends DatabaseServiceProxy<ResourceType> implements ResourceTypeService  {
    private final ResourceTypeRepository resourceTypeRepository;

    /**
     * Create an instance from the resourceTypeRepository.
     *
     * @param resourceTypeRepository the resource type repository
     */
    public ResourceTypeServiceImpl(ResourceTypeRepository resourceTypeRepository) {
        super(resourceTypeRepository, ResourceType.class);
        this.resourceTypeRepository = resourceTypeRepository;
    }

    @Override
    public Future<Boolean> existsOneByResourceType(String resourceType) {
        return Future
            .fromCompletionStage(resourceTypeRepository.findByResourceType(resourceType))
            .map(Objects::nonNull);
    }
}
