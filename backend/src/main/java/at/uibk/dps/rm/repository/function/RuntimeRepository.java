package at.uibk.dps.rm.repository.function;

import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

/**
 * Implements database operations for the runtime entity.
 *
 * @author matthi-g
 */
public class RuntimeRepository extends Repository<Runtime> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public RuntimeRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Runtime.class);
    }
}
