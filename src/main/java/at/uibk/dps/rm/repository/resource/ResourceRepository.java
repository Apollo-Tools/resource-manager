package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.repository.resource.entity.Resource;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class ResourceRepository  extends Repository<Resource> {

    public ResourceRepository(Stage.SessionFactory sessionFactory,
        Class<Resource> entityClass) {
        super(sessionFactory, entityClass);
    }

    @Override
    public CompletionStage<Resource> findById(long id) {
        return sessionFactory.withSession(session -> session.createQuery(
            "from Resource r left join fetch r.resourceType where r.resourceId =:id", entityClass)
                .setParameter("id", id)
                .getSingleResultOrNull()
            );
    }

    public CompletionStage<Resource> findByUrl(String url) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from Resource r where r.url=:url", entityClass)
                .setParameter("url", url)
                .getSingleResultOrNull()
        );
    }

    @Override
    public CompletionStage<List<Resource>> findAll() {
        return sessionFactory.withSession(session ->
            session.createQuery("from Resource r left join fetch r.resourceType", entityClass)
                .getResultList()
            );
    }
}
