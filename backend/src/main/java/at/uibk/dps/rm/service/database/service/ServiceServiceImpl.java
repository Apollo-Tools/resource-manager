package at.uibk.dps.rm.service.database.service;

import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletionStage;

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
    public ServiceServiceImpl(ServiceRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, Service.class, sessionFactory);
        this.serviceRepository = repository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        CompletionStage<Service> findOne = withSession(session -> serviceRepository.findByIdAndFetch(session, id));
        return Future.fromCompletionStage(findOne)
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonArray> findAll() {
        CompletionStage<List<Service>> findAll = withSession(serviceRepository::findAllAndFetch);
        return Future.fromCompletionStage(findAll)
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
        CompletionStage<Service> findOne = withSession(session -> serviceRepository.findOneByName(session, name));
        return Future.fromCompletionStage(findOne)
            .map(Objects::nonNull);
    }

    @Override
    public Future<Boolean> existsAllByIds(Set<Long> serviceIds) {
        CompletionStage<List<Service>> findAll = withSession(session ->
            serviceRepository.findAllByIds(session, serviceIds));
        return Future.fromCompletionStage(findAll)
            .map(result -> result.size() == serviceIds.size());
    }
}
