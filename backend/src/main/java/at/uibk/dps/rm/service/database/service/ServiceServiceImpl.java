package at.uibk.dps.rm.service.database.service;

import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

/**
 * This is the implementation of the #ServiceService.
 *
 * @author matthi-g
 */
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
    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(serviceRepository.findByIdAndFetch(id))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonArray> findAll() {
        return Future
            .fromCompletionStage(serviceRepository.findAllAndFetch())
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Service entity: result) {
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
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
