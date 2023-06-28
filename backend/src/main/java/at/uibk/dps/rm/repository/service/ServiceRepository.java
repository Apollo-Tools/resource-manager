package at.uibk.dps.rm.repository.service;

import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.entity.model.ServiceType;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.Repository;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.Session;
import org.hibernate.reactive.util.impl.CompletionStages;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Implements database operations for the service entity.
 *
 * @author matthi-g
 */
public class ServiceRepository extends Repository<Service> {

    /**
     * Create an instance.
     */
    public ServiceRepository() {
        super(Service.class);
    }

    /**
     * Find a service by its name.
     *
     * @param session the database session
     * @param name the name of the service
     * @return a CompletionStage that emits the service if it exists, else null
     */
    public CompletionStage<Service> findOneByName(Session session, String name) {
        return session.createQuery("from Service s where s.name=:name", entityClass)
            .setParameter("name", name)
            .getSingleResultOrNull();
    }

    /**
     * Find a service by its id and fetch the service type.
     *
     * @param session the database session
     * @param id the id of the service
     * @return a CompletionStage that emits the service if it exists, else null
     */
    public CompletionStage<Service> findByIdAndFetch(Session session, long id) {
        return session.createQuery("from Service s " +
                "left join fetch s.serviceType " +
                "where s.serviceId=:serviceId", entityClass)
            .setParameter("serviceId", id)
            .getSingleResultOrNull();
    }

    /**
     * Find all services by the serviceIds.
     *
     * @param session the database session
     * @param serviceIds the list of service ids
     * @return a CompletionStage that emits a list of services
     */
    public CompletionStage<List<Service>> findAllByIds(Session session, Set<Long> serviceIds) {
        if (serviceIds.isEmpty()) {
            return CompletionStages.completedFuture(new ArrayList<>());
        }

        String serviceIdsConcat = serviceIds.stream().map(Object::toString).collect(Collectors.joining(","));
        return session.createQuery("select distinct s from Service s " +
                "where s.serviceId in (" + serviceIdsConcat + ")", entityClass)
            .getResultList();
    }

    /**
     * Find all services and fetch the resource type.
     *
     * @param session the database session
     * @return a CompletionStage that emits a list of all services
     */
    public CompletionStage<List<Service>> findAllAndFetch(Session session) {
        return session.createQuery("from Service s left join fetch s.serviceType", entityClass)
            .getResultList();
    }

    @Override
    public CompletionStage<Service> update(Session session, long serviceId, JsonObject fields) {
        return session.createQuery("from Service s left join fetch s.serviceType " +
                "where s.serviceId=:serviceId ", entityClass)
            .setParameter("serviceId", serviceId)
            .getSingleResultOrNull()
            .thenCompose(service -> {
                if (service == null) {
                    throw new NotFoundException(entityClass);
                }
                CompletionStage<Void> serviceTypeCompletionStage = CompletionStages.voidFuture();
                if (fields.containsKey("service_type")) {
                    serviceTypeCompletionStage = session.find(ServiceType.class, fields.getJsonObject(
                        "service_type").getLong("service_type_id"))
                        .thenAccept(serviceType -> {
                            if (serviceType == null) throw new NotFoundException(ServiceType.class);
                        });
                }
                JsonObject jsonObject = JsonObject.mapFrom(service);
                fields.stream().forEach(entry -> jsonObject.put(entry.getKey(), entry.getValue()));
                Service updatedEntity = jsonObject.mapTo(entityClass);
                return serviceTypeCompletionStage.thenCompose(result -> session.merge(updatedEntity));
            });
    }
}
