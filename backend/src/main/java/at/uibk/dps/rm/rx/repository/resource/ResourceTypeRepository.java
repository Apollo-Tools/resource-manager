package at.uibk.dps.rm.rx.repository.resource;

import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.rx.repository.Repository;

/**
 * Implements database operations for the resource_type entity.
 *
 * @author matthi-g
 */
public class ResourceTypeRepository extends Repository<ResourceType> {

    /**
     * Create an instance.
     */
    public ResourceTypeRepository() {
        super(ResourceType.class);
    }
}
