package at.uibk.dps.rm.service.database.service;

import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;

import java.util.Objects;
import java.util.Set;

public class ServiceServiceImpl extends DatabaseServiceProxy<Service> implements ServiceService {

    private final ServiceRepository serviceRepository;

    /**
     * Create an instance from the repository.
     *
     * @param repository  the repository
     */
    public ServiceServiceImpl(ServiceRepository repository) {
        super(repository, Service.class);
        this.serviceRepository = repository;
    }

    @Override
    public Future<Boolean> existsOneByName(String name) {
        return Future
            .fromCompletionStage(serviceRepository.findOneByName(name))
            .map(Objects::nonNull);
    }

    @Override
    public Future<Boolean> existsAllByIds(Set<Long> serviceIds) {
        return Future
            .fromCompletionStage(serviceRepository.findAllByIds(serviceIds))
            .map(result -> result.size() == serviceIds.size());
    }
}
