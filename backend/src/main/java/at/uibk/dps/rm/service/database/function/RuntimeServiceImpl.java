package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.repository.RuntimeRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;

public class RuntimeServiceImpl extends ServiceProxy<Runtime> implements RuntimeService {
    private final RuntimeRepository runtimeRepository;

    public RuntimeServiceImpl(RuntimeRepository repository) {
        super(repository, Runtime.class);
        runtimeRepository = repository;
    }
}
