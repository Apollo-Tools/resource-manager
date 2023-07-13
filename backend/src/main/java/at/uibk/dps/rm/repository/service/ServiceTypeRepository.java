package at.uibk.dps.rm.repository.service;

import at.uibk.dps.rm.entity.model.ServiceType;
import at.uibk.dps.rm.repository.Repository;

/**
 * Implements database operations for the service_type entity.
 *
 * @author matthi-g
 */
public class ServiceTypeRepository extends Repository<ServiceType> {
    /**
     * Create an instance.
     */
    public ServiceTypeRepository() {
        super(ServiceType.class);
    }
}
