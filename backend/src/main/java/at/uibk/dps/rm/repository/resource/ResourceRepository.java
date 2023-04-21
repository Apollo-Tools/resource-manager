package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Implements database operations for the resource entity.
 *
 * @author matthi-g
 */
public class ResourceRepository extends Repository<Resource> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
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

    public CompletionStage<List<Resource>> findAllBySLOs(long functionId, List<String> metrics,
                                                              List<String> regions, List<Long> providerIds,
                                                              List<Long> resourceTypeIds) {
        String regionsCondition = "";
        if (!regions.isEmpty()) {
            regionsCondition = " and (" + regions.stream()
                .map(region -> "UPPER(reg.name) like UPPER('%" + region + "%')")
                .collect(Collectors.joining("or ")) + ") ";
        }
        String providerCondition = "";
        if (!providerIds.isEmpty()) {
            providerCondition = " and reg.resourceProvider.providerId in (" +
                providerIds.stream().map(Object::toString).collect(Collectors.joining(",")) + ") ";
        }
        String resourceTypeCondition = "";
        if (!resourceTypeIds.isEmpty()) {
            resourceTypeCondition = " and rt in (" +
                resourceTypeIds.stream().map(Object::toString).collect(Collectors.joining(",")) + ") ";
        }
        String metricsCondition = "";
        if (!metrics.isEmpty()) {
            metricsCondition = " and m.metric in (" +
                metrics.stream().map(metric -> "'" + metric + "'").collect(Collectors.joining(",")) + ") ";
        }

        String query = "select distinct r from FunctionResource fr " +
            "left join fr.resource r " +
            "left join fetch r.metricValues mv " +
            "left join fetch r.resourceType rt " +
            "left join fetch r.region reg " +
            "left join fetch reg.resourceProvider " +
            "left join fetch mv.metric m " +
            "where fr.function.functionId=:functionId " + regionsCondition + providerCondition + resourceTypeCondition +
            metricsCondition;

        return this.sessionFactory.withSession(session ->
            session.createQuery(query, Resource.class)
                .setParameter("functionId", functionId)
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
