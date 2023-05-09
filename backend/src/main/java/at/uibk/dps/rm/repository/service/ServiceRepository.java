package at.uibk.dps.rm.repository.service;

import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class ServiceRepository extends Repository<Service> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public ServiceRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Service.class);
    }

    public CompletionStage<Service> findOneByName(String name) {
        return sessionFactory.withSession(session -> session.createQuery(
                "from Service s " +
                    "where s.name=:name", entityClass)
            .setParameter("name", name)
            .getSingleResultOrNull()
        );
    }

    public CompletionStage<List<Service>> findAllByIds(Set<Long> serviceIds) {
        String serviceIdsConcat = serviceIds.stream().map(Object::toString).collect(Collectors.joining(","));
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct s from Service s " +
                    "where s.serviceId in (" + serviceIdsConcat + ")", entityClass)
                .getResultList()
        );
    }
}
