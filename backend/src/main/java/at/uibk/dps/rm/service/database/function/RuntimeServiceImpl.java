package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.repository.function.RuntimeRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;

import java.util.Objects;

public class RuntimeServiceImpl extends ServiceProxy<Runtime> implements RuntimeService {
    private final RuntimeRepository runtimeRepository;

    public RuntimeServiceImpl(RuntimeRepository repository) {
        super(repository, Runtime.class);
        runtimeRepository = repository;
    }

    @Override
    public Future<Boolean> existsOneByName(String name) {
        return Future
            .fromCompletionStage(runtimeRepository.findByName(name))
            .map(Objects::nonNull);
    }
}
