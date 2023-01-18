package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.repository.FunctionResourceRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;

public class FunctionResourceServiceImpl extends ServiceProxy<FunctionResource> implements FunctionResourceService {
    private final FunctionResourceRepository functionResourceRepository;

    public FunctionResourceServiceImpl(FunctionResourceRepository repository) {
        super(repository, FunctionResource.class);
        functionResourceRepository = repository;
    }
}
