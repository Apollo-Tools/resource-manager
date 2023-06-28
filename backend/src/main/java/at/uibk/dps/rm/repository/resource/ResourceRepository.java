package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;
import org.hibernate.reactive.util.impl.CompletionStages;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Implements database operations for the resource entity.
 *
 * @author matthi-g
 */
public class ResourceRepository extends Repository<Resource> {

    /**
     * Create an instance.
     */
    public ResourceRepository() {
        super(Resource.class);
    }

    /**
     * Find a resource by its id and fetch the resource type, platform, environment, region, metric values and
     * resource provider.
     *
     * @param session the database session
     * @param id the id of the resource
     * @return a CompletionStage that emits the resource if it exists, else null
     */
    public CompletionStage<Resource> findByIdAndFetch(Session session, long id) {
        return session.createQuery(
                "from Resource r " +
                "left join fetch r.metricValues mv " +
                "left join fetch mv.metric " +
                "left join fetch r.region reg " +
                "left join fetch reg.resourceProvider rp " +
                "left join fetch rp.environment " +
                "left join fetch r.platform p " +
                "left join fetch p.resourceType " +
                "where r.resourceId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull();
    }

    /**
     * Find all resources and fetch the resource type, platform, environment, region, metric values and
     * resource provider.
     *
     * @param session the database session
     * @return a CompletionStage that emits a list of all resources
     */
    public CompletionStage<List<Resource>> findAllAndFetch(Session session) {
        return session.createQuery("select distinct r from Resource r " +
                "left join fetch r.metricValues mv " +
                "left join fetch mv.metric " +
                "left join fetch r.region reg " +
                "left join fetch reg.resourceProvider rp " +
                "left join fetch rp.environment " +
                "left join fetch r.platform p " +
                "left join fetch p.resourceType ", entityClass)
            .getResultList();
    }

    /**
     * Find all resources by their metrics, resource types, regions and resource providers.
     *
     * @param session the database session
     * @param metrics the ids of the metrics
     * @param regionIds the ids of the regions
     * @param providerIds the ids of the resource providers
     * @param resourceTypeIds the ids of the resource types
     * @return a CompletionStage that emits a list of all resources
     */
    public CompletionStage<List<Resource>> findAllBySLOs(Session session, List<String> metrics,
        List<Long> environmentIds, List<Long> resourceTypeIds, List<Long> platformIds, List<Long> regionIds,
            List<Long> providerIds) {
        List<String> conditions = new ArrayList<>();
        if (!environmentIds.isEmpty()) {
            conditions.add("e.environmentId in (" +
                environmentIds.stream().map(Object::toString).collect(Collectors.joining(",")) + ")");
        }
        if (!resourceTypeIds.isEmpty()) {
            conditions.add("rt.typeId in (" +
                resourceTypeIds.stream().map(Object::toString).collect(Collectors.joining(",")) + ")");
        }
        if (!platformIds.isEmpty()) {
            conditions.add("p.platformId in (" +
                platformIds.stream().map(Object::toString).collect(Collectors.joining(",")) + ")");
        }
        if (!regionIds.isEmpty()) {
            conditions.add("reg.regionId in (" +
                regionIds.stream().map(Object::toString).collect(Collectors.joining(",")) + ")");
        }
        if (!providerIds.isEmpty()) {
            conditions.add("rp.providerId in (" +
                providerIds.stream().map(Object::toString).collect(Collectors.joining(",")) + ")");
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
            "left join fetch mv.metric m " +
            "left join fetch r.region reg " +
            "left join fetch reg.resourceProvider rp " +
            "left join fetch rp.environment e " +
            "left join fetch r.platform p " +
            "left join fetch p.resourceType rt " +
            conditionString;

        return session.createQuery(query, Resource.class)
            .getResultList();
    }

    /**
     * Find all resources by their resource type.
     *
     * @param session the database session
     * @param typeId the id of the resource type
     * @return a CompletionStage that emits a list of resources
     */
    public CompletionStage<List<Resource>> findByResourceType(Session session, long typeId) {
        return session.createQuery("from Resource r " +
                "where r.platform.resourceType.typeId=:typeId", entityClass)
            .setParameter("typeId", typeId)
            .getResultList();
    }

    /**
     * Find all resources by an ensemble and fetch the resource, resourceType, region,
     * resourceProvider, platform, environment, metricValues and metric.
     *
     * @param session the database session
     * @param ensembleId the id of the ensemble
     * @return a CompletionStage that emits a list of resources
     */
    public CompletionStage<List<Resource>> findAllByEnsembleId(Session session, long ensembleId) {
        return session.createQuery("select distinct r from ResourceEnsemble re " +
                "left join re.resource r " +
                "left join fetch r.metricValues mv " +
                "left join fetch mv.metric " +
                "left join fetch r.region reg " +
                "left join fetch reg.resourceProvider rp " +
                "left join fetch rp.environment " +
                "left join fetch r.platform p " +
                "left join fetch p.resourceType " +
                "where re.ensemble.ensembleId=:ensembleId", entityClass)
            .setParameter("ensembleId", ensembleId)
            .getResultList();
    }

    /**
     * Find all resources by the resourceIds and resourceTypes.
     *
     * @param session the database session
     * @param resourceIds the list of resource ids
     * @param resourceTypes the list resource types
     * @return a CompletionStage that emits a list of resources
     */
    public CompletionStage<List<Resource>> findAllByResourceIdsAndResourceTypes(Session session, Set<Long> resourceIds,
        List<String> resourceTypes) {
        if (resourceIds.isEmpty()) {
            return CompletionStages.completedFuture(new ArrayList<>());
        }
        String resourceIdsConcat = resourceIds.stream().map(Object::toString).collect(Collectors.joining(","));
        String resourceTypesConcat = resourceTypes.stream().map(Object::toString).collect(Collectors.joining("','"));
        return session.createQuery("select distinct r from Resource r " +
                "where r.resourceId in (" + resourceIdsConcat + ") and " +
                "r.platform.resourceType.resourceType in ('" + resourceTypesConcat + "')", entityClass)
            .getResultList();
    }

    /**
     * Find all resources by the resourceIds and fetch the region, resourceProvider, resourceType,
     * platform, environment, metricValues and metric.
     *
     * @param session the datbase session
     * @param resourceIds the list of resource ids
     * @return a CompletionStage that emits a list of resources
     */
    public CompletionStage<List<Resource>> findAllByResourceIdsAndFetch(Session session, List<Long> resourceIds) {
        String resourceIdsConcat = resourceIds.stream().map(Object::toString).collect(Collectors.joining(","));
        return session.createQuery("select distinct r from Resource r " +
                "left join fetch r.metricValues mv " +
                "left join fetch mv.metric " +
                "left join fetch r.region reg " +
                "left join fetch reg.resourceProvider rp " +
                "left join fetch rp.environment " +
                "left join fetch r.platform p " +
                "left join fetch p.resourceType " +
                "where r.resourceId in (" + resourceIdsConcat + ")", Resource.class)
            .getResultList();
    }
}
