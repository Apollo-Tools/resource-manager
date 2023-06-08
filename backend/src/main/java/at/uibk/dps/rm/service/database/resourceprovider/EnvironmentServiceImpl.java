package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.Environment;
import at.uibk.dps.rm.repository.resourceprovider.EnvironmentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;

public class EnvironmentServiceImpl extends DatabaseServiceProxy<Environment> implements EnvironmentService {
    /**
     * Create an instance from the repository.
     *
     * @param repository the region repository
     */
    public EnvironmentServiceImpl(EnvironmentRepository repository) {
        super(repository, Environment.class);
    }
}
