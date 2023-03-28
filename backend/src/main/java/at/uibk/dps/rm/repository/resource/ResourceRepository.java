package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class ResourceRepository extends Repository<Resource> {

    public ResourceRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Resource.class);
    }

    public CompletionStage<Resource> findByIdAndFetch(long id) {
        return sessionFactory.withSession(session -> session.createQuery(
                "from Resource r " +
                "left join fetch r.resourceType " +
                "left join fetch r.region " +
                "left join fetch r.region.resourceProvider " +
                "where r.resourceId =:id", entityClass)
                .setParameter("id", id)
                .getSingleResultOrNull()
            );
    }

    public CompletionStage<List<Resource>> findAllAndFetch() {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct r from Resource r " +
                    "left join fetch r.metricValues mv " +
                    "left join fetch mv.metric " +
                    "left join fetch r.region reg " +
                    "left join fetch reg.resourceProvider " +
                    "left join fetch r.resourceType ", entityClass)
                .getResultList()
            );
    }

    public CompletionStage<List<Resource>> findByFunctionAndMultipleMetricsAndFetch(long functionId, List<String> metrics) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("select distinct r from FunctionResource fr " +
                    "left join fr.resource r " +
                    "left join fetch r.metricValues mv " +
                    "left join fetch r.resourceType " +
                    "left join fetch r.region reg " +
                    "left join fetch reg.resourceProvider " +
                    "left join fetch mv.metric m " +
                    "where fr.function.functionId=:functionId and m.metric in :metrics", Resource.class)
                .setParameter("functionId", functionId)
                .setParameter("metrics", metrics)
                .getResultList()
        );
    }

    public CompletionStage<List<Resource>> findByResourceType(long typeId) {
        return sessionFactory.withSession(session ->
            session.createQuery("from Resource r " +
                    "where r.resourceType.typeId=:typeId", entityClass)
                .setParameter("typeId", typeId)
                .getResultList());
    }

    public CompletionStage<List<Resource>> findAllByFunctionIdAndFetch(long functionId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("select distinct r from FunctionResource fr " +
                    "left join fr.function f " +
                    "left join fr.resource r " +
                    "left join fetch r.region reg " +
                    "left join fetch reg.resourceProvider " +
                    "left join fetch r.resourceType " +
                    "left join fetch r.metricValues mv " +
                    "left join fetch mv.metric " +
                    "where f.functionId=:functionId", Resource.class)
                .setParameter("functionId", functionId)
                .getResultList()
        );
    }
}
