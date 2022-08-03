package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.repository.resource.ResourceTypeRepository;
import at.uibk.dps.rm.repository.resource.entity.ResourceType;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;

import java.util.Objects;

public class ResourceTypeServiceImpl extends ServiceProxy<ResourceType> implements ResourceTypeService  {
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
