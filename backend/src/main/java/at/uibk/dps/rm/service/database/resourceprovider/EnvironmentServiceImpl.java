package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.Environment;
import at.uibk.dps.rm.repository.resourceprovider.EnvironmentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

/**
 * This is the implementation of the #EnvironmentService.
 *
 * @author matthi-g
 */
public class EnvironmentServiceImpl extends DatabaseServiceProxy<Environment> implements EnvironmentService {
    /**
     * Create an instance from the repository.
     *
     * @param repository the region repository
     */
    public EnvironmentServiceImpl(EnvironmentRepository repository, SessionManagerProvider smProvider) {
        super(repository, Environment.class, smProvider);
    }
}
