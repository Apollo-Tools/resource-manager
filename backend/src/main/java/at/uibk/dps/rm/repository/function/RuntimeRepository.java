package at.uibk.dps.rm.repository.function;

import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.repository.Repository;

/**
 * Implements database operations for the runtime entity.
 *
 * @author matthi-g
 */
public class RuntimeRepository extends Repository<Runtime> {

    /**
     * Create an instance.
     */
    public RuntimeRepository() {
        super(Runtime.class);
    }
}
