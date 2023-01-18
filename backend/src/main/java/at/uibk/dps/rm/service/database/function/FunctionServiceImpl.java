package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.repository.FunctionRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;

public class FunctionServiceImpl extends ServiceProxy<Function> implements FunctionService {
    private final FunctionRepository functionRepository;

    public FunctionServiceImpl(FunctionRepository repository) {
        super(repository, Function.class);
        functionRepository = repository;
    }
}
