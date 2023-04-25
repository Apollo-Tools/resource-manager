package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
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

    /**
     * Find a resource by its id and fetch the resource type, region and resource provider.
     *
     * @param id the id of the resource
     * @return a CompletionStage that emits the resource if it exists, else null
     */
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

    /**
     * Find all resources and fetch the metric values, metric, region and resource provider.
     *
     * @return a CompletionStage that emits a list of all resources
     */
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

    /**
     * Find all resources by their metrics, resource types, regions and resource providers.
     *
     * @param metrics the ids of the metrics
     * @param regionIds the ids of the regions
     * @param providerIds the ids of the resource providers
     * @param resourceTypeIds the ids of the resource types
     * @return a CompletionStage that emits a list of all resources
     */
    public CompletionStage<List<Resource>> findAllBySLOs(List<String> metrics, List<Long> regionIds,
            List<Long> providerIds, List<Long> resourceTypeIds) {
        List<String> conditions = new ArrayList<>();
        if (!regionIds.isEmpty()) {
            conditions.add("reg.regionId in (" +
                    regionIds.stream().map(Object::toString).collect(Collectors.joining(",")) + ")");
        }
        if (!providerIds.isEmpty()) {
            conditions.add("reg.resourceProvider.providerId in (" +
                providerIds.stream().map(Object::toString).collect(Collectors.joining(",")) + ")");
        }
        if (!resourceTypeIds.isEmpty()) {
            conditions.add("rt.typeId in (" +
                resourceTypeIds.stream().map(Object::toString).collect(Collectors.joining(",")) + ")");
        }
        if (!metrics.isEmpty()) {
            conditions.add("m.metric in (" +
                metrics.stream().map(metric -> "'" + metric + "'").collect(Collectors.joining(",")) + ")");
        }
        String conditionString ="";
        if (!conditions.isEmpty()) {
            conditionString = "where " + String.join(" and ", conditions);
        }

        String query = "select distinct r from Resource r " +
            "left join fetch r.metricValues mv " +
            "left join fetch r.resourceType rt " +
            "left join fetch r.region reg " +
            "left join fetch reg.resourceProvider " +
            "left join fetch mv.metric m " +
            conditionString;

        return this.sessionFactory.withSession(session ->
            session.createQuery(query, Resource.class)
                .getResultList()
        );
    }

    /**
     * Find all resources by their resource type.
     *
     * @param typeId the id of the resource type
     * @return a CompletionStage that emits a list of resources
     */
    public CompletionStage<List<Resource>> findByResourceType(long typeId) {
        return sessionFactory.withSession(session ->
            session.createQuery("from Resource r " +
                    "where r.resourceType.typeId=:typeId", entityClass)
                .setParameter("typeId", typeId)
                .getResultList());
    }

    /**
     * Find all resources by their function and fetch the region, resource provider, resource type,
     * metric values and metrics.
     *
     * @param functionId the id of the function
     * @return a CompletionStage that emits a list of all resources
     */
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
