package at.uibk.dps.rm.service.database.service;

import at.uibk.dps.rm.entity.model.ServiceType;
import at.uibk.dps.rm.repository.service.ServiceTypeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;

/**
 * This is the implementation of the #ServiceTypeService.
 *
 * @author matthi-g
 */
public class ServiceTypeServiceImpl extends DatabaseServiceProxy<ServiceType> implements ServiceTypeService {
    /**
     * Create an instance from the repository.
     *
     * @param repository  the repository
     */
    public ServiceTypeServiceImpl(ServiceTypeRepository repository) {
        super(repository, ServiceType.class);
    }
}
