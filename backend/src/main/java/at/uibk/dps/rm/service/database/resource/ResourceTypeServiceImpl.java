package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.repository.resource.ResourceTypeRepository;
import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;

import java.util.Objects;

public class ResourceTypeServiceImpl extends DatabaseServiceProxy<ResourceType> implements ResourceTypeService  {
    private final ResourceTypeRepository resourceTypeRepository;

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
