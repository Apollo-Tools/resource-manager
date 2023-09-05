package at.uibk.dps.rm.repository.resourceprovider;

import at.uibk.dps.rm.entity.model.Environment;
import at.uibk.dps.rm.repository.Repository;

/**
 * Implements database operations for the environment entity.
 *
 * @author matthi-g
 */
public class EnvironmentRepository extends Repository<Environment> {
    /**
     * Create an instance.
     */
    public EnvironmentRepository() {
        super(Environment.class);
    }
}
