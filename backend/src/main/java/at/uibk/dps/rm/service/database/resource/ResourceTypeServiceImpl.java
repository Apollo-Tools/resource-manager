package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.repository.resource.ResourceTypeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import org.hibernate.reactive.stage.Stage;

/**
 * This is the implementation of the {@link ResourceTypeService}.
 *
 * @author matthi-g
 */
public class ResourceTypeServiceImpl extends DatabaseServiceProxy<ResourceType> implements ResourceTypeService {

    /**
     * Create an instance from the resourceTypeRepository.
     *
     * @param resourceTypeRepository the resource type repository
     */
    public ResourceTypeServiceImpl(ResourceTypeRepository resourceTypeRepository, Stage.SessionFactory sessionFactory) {
        super(resourceTypeRepository, ResourceType.class, sessionFactory);
    }
}
