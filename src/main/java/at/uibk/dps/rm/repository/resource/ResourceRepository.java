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

    public CompletionStage<Resource> findByIdAndFetch(long id) {
        return sessionFactory.withSession(session -> session.createQuery(
            "from Resource r left join fetch r.resourceType where r.resourceId =:id", entityClass)
                .setParameter("id", id)
                .getSingleResultOrNull()
            );
    }

    @Deprecated
    public CompletionStage<Resource> findByUrl(String url) {
        // TODO: Remove
        return this.sessionFactory.withSession(session ->
            session.createQuery("from Resource r where r.id=1", entityClass)
                .getSingleResultOrNull()
        );
    }

    public CompletionStage<List<Resource>> findAllAndFetch() {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct r from Resource r " +
                            "left join fetch  r.metricValues " +
                            "left join fetch r.resourceType " , entityClass)
                .getResultList()
            );
    }

    public CompletionStage<List<Resource>> findByMultipleMetricsAndFetch(List<String> metrics) {
        return this.sessionFactory.withSession(session ->
                session.createQuery("select distinct r from Resource r "
                                + "left join fetch r.metricValues "
                                + "left join fetch r.resourceType "
                                + "where r.resourceId in ("
                                + "select mv.resource.resourceId from MetricValue mv "
                                + "where mv.metric.metric in :metrics "
                                + "group by mv.resource.resourceId "
                                + "having count(mv.metric.metric) = :metricAmount)", Resource.class)
                        .setParameter("metrics", metrics)
                        .setParameter("metricAmount", (long) metrics.size())
                        .getResultList()
        );
    }

    public CompletionStage<List<Resource>> findByResourceType(long typeId) {
        return sessionFactory.withSession(session ->
            session.createQuery("from Resource r where r.resourceType.typeId=:typeId", entityClass)
                .setParameter("typeId", typeId)
                .getResultList());
    }
}
