package at.uibk.dps.rm.repository.service;

import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;
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
     * @param typeId the id of the service type
     * @param accountId the id of the account
     * @return a CompletionStage that emits the service if it exists, else null
     */
    public CompletionStage<Service> findOneByNameTypeAndCreator(Session session, String name, long typeId,
            long accountId) {
        return session.createQuery("from Service s " +
                "where s.name=:name and s.serviceType.artifactTypeId=:typeId and s.createdBy.accountId=:accountId",
                entityClass)
            .setParameter("name", name)
            .setParameter("typeId", typeId)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull();
    }

    /**
     * Find a service by its id and fetch the service type and k8s service type.
     *
     * @param session the database session
     * @param id the id of the service
     * @return a CompletionStage that emits the service if it exists, else null
     */
    public CompletionStage<Service> findByIdAndFetch(Session session, long id) {
        return session.createQuery("from Service s " +
                "left join fetch s.serviceType " +
                "left join fetch s.k8sServiceType " +
                "where s.serviceId=:serviceId", entityClass)
            .setParameter("serviceId", id)
            .getSingleResultOrNull();
    }

    /**
     * Find a service by its id and accountId.
     *
     * @param session the database session
     * @param serviceId the id of the service
     * @param accountId the id of the owner
     * @param includePublic whether to include public services
     * @return a CompletionStage that emits the entity if it exists, else null
     */
    public CompletionStage<Service> findByIdAndAccountId(Session session, long serviceId, long accountId,
            boolean includePublic) {
        return session.createQuery("from Service s " +
                "left join fetch s.serviceType " +
                "left join fetch s.k8sServiceType " +
                "left join fetch s.createdBy " +
                "where s.serviceId=:serviceId and " +
                "(s.createdBy.accountId=:accountId or (:includePublic=true and s.isPublic=true))", entityClass)
            .setParameter("serviceId", serviceId)
            .setParameter("accountId", accountId)
            .setParameter("includePublic", includePublic)
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
     * Find all services and fetch the resource type and k8s resource type.
     *
     * @param session the database session
     * @return a CompletionStage that emits a list of all services
     */
    public CompletionStage<List<Service>> findAllAndFetch(Session session) {
        return session.createQuery("from Service s left join fetch s.serviceType", entityClass)
            .getResultList();
    }

    /**
     * Find all public services and fetch the resource type and k8s resource type.
     *
     * @param session the database session
     * @return a CompletionStage that emits a list of all services
     */
    public CompletionStage<List<Service>> findAllPublicAndFetch(Session session) {
        return session.createQuery("from Service s " +
                    "left join fetch s.serviceType " +
                    "left join fetch s.createdBy " +
                    "where s.isPublic = true",
                entityClass).getResultList();
    }

    @Override
    public CompletionStage<List<Service>> findAllByAccountId(Stage.Session session, long accountId) {
        return session.createQuery("from Service s " +
                "left join fetch s.serviceType " +
                "left join fetch s.createdBy " +
                "where s.createdBy.accountId=:accountId", entityClass)
            .setParameter("accountId", accountId)
            .getResultList();
    }
}
