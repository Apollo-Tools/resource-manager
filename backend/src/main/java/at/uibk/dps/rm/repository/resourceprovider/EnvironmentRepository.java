package at.uibk.dps.rm.repository.resourceprovider;

import at.uibk.dps.rm.entity.model.Environment;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

/**
 * Implements database operations for the environment entity.
 *
 * @author matthi-g
 */
public class EnvironmentRepository extends Repository<Environment> {
    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public EnvironmentRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Environment.class);
    }
}
