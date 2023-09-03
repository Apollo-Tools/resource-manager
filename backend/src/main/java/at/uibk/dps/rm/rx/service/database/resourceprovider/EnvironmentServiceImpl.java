package at.uibk.dps.rm.rx.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.Environment;
import at.uibk.dps.rm.rx.repository.resourceprovider.EnvironmentRepository;
import at.uibk.dps.rm.rx.service.database.DatabaseServiceProxy;
import org.hibernate.reactive.stage.Stage;

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
    public EnvironmentServiceImpl(EnvironmentRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, Environment.class, sessionFactory);
    }
}
