package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.repository.function.RuntimeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

/**
 * This is the implementation of the #RuntimeService.
 *
 * @author matthi-g
 */
public class RuntimeServiceImpl extends DatabaseServiceProxy<Runtime> implements RuntimeService {

    /**
     * Create an instance from the repository.
     *
     * @param repository the runtime repository
     */
    public RuntimeServiceImpl(RuntimeRepository repository, SessionManagerProvider smProvider) {
        super(repository, Runtime.class, smProvider);
    }
}
