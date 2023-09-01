package at.uibk.dps.rm.rx.repository.service;

import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.rx.repository.Repository;
import at.uibk.dps.rm.rx.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
     * @param sessionManager the database session manager
     * @param name the name of the service
     * @param typeId the id of the service type
     * @param accountId the id of the account
     * @return a Maybe that emits the service if it exists, else null
     */
    public Maybe<Service> findOneByNameTypeAndCreator(SessionManager sessionManager, String name, long typeId,
            long accountId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from Service s " +
                "where s.name=:name and s.serviceType.artifactTypeId=:typeId and s.createdBy.accountId=:accountId",
                entityClass)
            .setParameter("name", name)
            .setParameter("typeId", typeId)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find a service by its id and fetch the service type and k8s service type.
     *
     * @param sessionManager the database session manager
     * @param id the id of the service
     * @return a Maybe that emits the service if it exists, else null
     */
    public Maybe<Service> findByIdAndFetch(SessionManager sessionManager, long id) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from Service s " +
                "left join fetch s.serviceType " +
                "left join fetch s.k8sServiceType " +
                "where s.serviceId=:serviceId", entityClass)
            .setParameter("serviceId", id)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find a service by its id and accountId.
     *
     * @param sessionManager the database session manager
     * @param serviceId the id of the service
     * @param accountId the id of the owner
     * @param includePublic whether to include public services
     * @return a Maybe that emits the service if it exists, else null
     */
    public Maybe<Service> findByIdAndAccountId(SessionManager sessionManager, long serviceId, long accountId,
            boolean includePublic) {
        return Maybe.fromCompletionStage(
            sessionManager.getSession().createQuery("from Service s " +
                "left join fetch s.serviceType " +
                "left join fetch s.k8sServiceType " +
                "left join fetch s.createdBy " +
                "where s.serviceId=:serviceId and " +
                "(s.createdBy.accountId=:accountId or (:includePublic=true and s.isPublic=true))", entityClass)
            .setParameter("serviceId", serviceId)
            .setParameter("accountId", accountId)
            .setParameter("includePublic", includePublic)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find all services by the serviceIds.
     *
     * @param sessionManager the database session manager
     * @param serviceIds the list of service ids
     * @return a CompletionStage that emits a list of services
     */
    public Single<List<Service>> findAllByIds(SessionManager sessionManager, Set<Long> serviceIds) {
        if (serviceIds.isEmpty()) {
            return Single.just(new ArrayList<>());
        }

        String serviceIdsConcat = serviceIds.stream().map(Object::toString).collect(Collectors.joining(","));
        return Single.fromCompletionStage(
            sessionManager.getSession().createQuery("select distinct s from Service s " +
                "where s.serviceId in (" + serviceIdsConcat + ")", entityClass)
            .getResultList()
        );
    }

    /**
     * Find all services and fetch the resource type and k8s resource type.
     *
     * @param sessionManager the database session manager
     * @return a Single that emits a list of all services
     */
    public Single<List<Service>> findAllAndFetch(SessionManager sessionManager) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("from Service s left join fetch s.serviceType", entityClass)
            .getResultList()
        );
    }

    /**
     * Find all accessible services and fetch the resource type and k8s resource type.
     *
     * @param sessionManager the database session manager
     * @return a Single that emits a list of all services
     */
    public Single<List<Service>> findAllAccessibleAndFetch(SessionManager sessionManager, long accountId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("from Service s " +
                "left join fetch s.serviceType " +
                "left join fetch s.createdBy " +
                "where s.isPublic = true or s.createdBy.accountId=:accountId", entityClass)
            .setParameter("accountId", accountId)
            .getResultList()
        );
    }

    @Override
    public Single<List<Service>> findAllByAccountId(SessionManager sessionManager, long accountId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("from Service s " +
                "left join fetch s.serviceType " +
                "left join fetch s.createdBy " +
                "where s.createdBy.accountId=:accountId", entityClass)
            .setParameter("accountId", accountId)
            .getResultList()
        );
    }
}
