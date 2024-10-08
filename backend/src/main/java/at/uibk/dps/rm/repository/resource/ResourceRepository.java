package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.dto.resource.FindAllFunctionDeploymentScrapeTargetsDTO;
import at.uibk.dps.rm.entity.dto.resource.FindAllOpenFaaSScrapeTargetsDTO;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.model.MainResource;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.SubResource;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
     * Find a main resource by its name.
     *
     * @param sessionManager the database session manager
     * @param name the name of the resource
     * @return a Maybe that emits the resource, else null
     */
    public Maybe<Resource> findByName(SessionManager sessionManager, String name) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from MainResource r " +
                "where r.name=:name", entityClass)
            .setParameter("name", name)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find a resource by its id and fetch the resource type, platform, environment, region, metric
     * values and resource provider.
     *
     * @param sessionManager the database session manager
     * @param id the id of the resource
     * @return a CompletionStage that emits the resource if it exists, else null
     */
    public Maybe<Resource> findByIdAndFetch(SessionManager sessionManager, long id) {
        Maybe<Resource> getMainResource = Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery(
                "from MainResource r " +
                "left join fetch r.metricValues mv " +
                "left join fetch mv.metric " +
                "left join fetch r.region reg " +
                "left join fetch reg.resourceProvider rp " +
                "left join fetch rp.environment " +
                "left join fetch r.platform p " +
                "left join fetch p.resourceType " +
                "where r.resourceId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull()
        );

        Maybe<Resource> getSubResource = Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery(
                "from SubResource r " +
                "left join fetch r.mainResource mr " +
                "left join fetch mr.metricValues mmv " +
                "left join fetch mmv.metric " +
                "left join fetch mr.region reg " +
                "left join fetch reg.resourceProvider rp " +
                "left join fetch rp.environment " +
                "left join fetch mr.platform p " +
                "left join fetch p.resourceType " +
                "left join fetch r.metricValues mv " +
                "left join fetch mv.metric " +
                "where r.resourceId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull()
        );
        return getMainResource.switchIfEmpty(getSubResource);
    }

    /**
     * Find all main resources and fetch the resource type, platform, environment, region, metric values
     * and resource provider.
     *
     * @param sessionManager the database session manager
     * @return a Single that emits a list of all resources
     */
    public Single<List<Resource>> findAllMainResourcesAndFetch(SessionManager sessionManager) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct r from MainResource r " +
                "left join fetch r.region reg " +
                "left join fetch reg.resourceProvider rp " +
                "left join fetch rp.environment " +
                "left join fetch r.platform p " +
                "left join fetch p.resourceType " +
                "left join fetch r.subResources sr ", entityClass)
            .getResultList()
        );
    }

    /**
     * Find all main resources by platform and fetch the metric values.
     *
     * @param sessionManager the database session manager
     * @param platform the platform
     * @return a Single that emits a list of all resources
     */
    public Single<List<Resource>> findAllMainResourcesByPlatform(SessionManager sessionManager, String platform) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct r from MainResource r " +
                "left join fetch r.metricValues mv " +
                "left join fetch mv.metric m " +
                "where r.platform.platform=:platform", entityClass)
                .setParameter("platform", platform)
            .getResultList()
        );
    }

    /**
     * Find all main and sub resources and fetch the resource type, platform, environment, region,
     * metric values and resource provider.
     *
     * @param sessionManager the database session manager
     * @return a Single that emits a list of all resources
     */
    public Single<List<Resource>> findAllMainAndSubResourcesAndFetch(SessionManager sessionManager) {
        String mainQuery = "select distinct r from MainResource r " +
            "left join fetch r.metricValues mv " +
            "left join fetch mv.metric m " +
            "left join fetch r.region reg " +
            "left join fetch reg.resourceProvider rp " +
            "left join fetch rp.environment e " +
            "left join fetch r.platform p " +
            "left join fetch p.resourceType rt";

        String subQuery = "select distinct r from SubResource r " +
            "left join fetch r.metricValues mv " +
            "left join fetch mv.metric m " +
            "left join fetch r.mainResource mr " +
            "left join fetch mr.region reg " +
            "left join fetch reg.resourceProvider rp " +
            "left join fetch rp.environment e " +
            "left join fetch mr.platform p " +
            "left join fetch p.resourceType rt";

        return mergeMainAndSubResources(sessionManager, mainQuery, subQuery);
    }

    /**
     * Find all resources by their environment, resource types, platforms, regions and resource providers.
     *
     * @param sessionManager the database session manager
     * @param environmentIds the ids of the environments
     * @param resourceTypeIds the ids of the resource types
     * @param platformIds the ids of the platforms
     * @param regionIds the ids of the regions
     * @param providerIds the ids of the resource providers
     * @return a Single that emits a list of all resources
     */
    public Single<List<Resource>> findAllByNonMVSLOs(SessionManager sessionManager,
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
        String conditionString ="";
        if (!conditions.isEmpty()) {
            conditionString = "where " + String.join(" and ", conditions);
        }

        String mainQuery = "select distinct r from MainResource r " +
            "left join fetch r.metricValues mv " +
            "left join fetch mv.metric m " +
            "left join fetch r.region reg " +
            "left join fetch reg.resourceProvider rp " +
            "left join fetch rp.environment e " +
            "left join fetch r.platform p " +
            "left join fetch p.resourceType rt " +
            conditionString;

        String subQuery = "select distinct r from SubResource r " +
            "left join fetch r.metricValues mv " +
            "left join fetch mv.metric m " +
            "left join fetch r.mainResource mr " +
            "left join fetch mr.region reg " +
            "left join fetch reg.resourceProvider rp " +
            "left join fetch rp.environment e " +
            "left join fetch mr.platform p " +
            "left join fetch p.resourceType rt " +
            conditionString;

        return mergeMainAndSubResources(sessionManager, mainQuery, subQuery);
    }

    /**
     * Find all resources by an ensemble and fetch the resource, resourceType, region,
     * resourceProvider, platform, environment, metricValues and metric.
     *
     * @param sessionManager the database session manager
     * @param ensembleId the id of the ensemble
     * @return a Single that emits a list of resources
     */
    public Single<List<Resource>> findAllByEnsembleId(SessionManager sessionManager, long ensembleId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery(
            "select distinct r from ResourceEnsemble re " +
                "left join re.resource r " +
                "left join fetch r.metricValues mv " +
                "left join fetch mv.metric " +
                "left join fetch r.region reg " +
                "left join fetch reg.resourceProvider rp " +
                "left join fetch rp.environment " +
                "left join fetch r.platform p " +
                "left join fetch p.resourceType " +
                "left join fetch r.mainResource mr " +
                "left join fetch mr.metricValues mmv " +
                "left join fetch mmv.metric " +
                "left join fetch mr.region mreg " +
                "left join fetch mreg.resourceProvider mrp " +
                "left join fetch mrp.environment " +
                "left join fetch mr.platform mp " +
                "left join fetch mp.resourceType " +
                "where re.ensemble.ensembleId=:ensembleId", Resource.class)
            .setParameter("ensembleId", ensembleId)
            .getResultList()
        );
    }

    /**
     * Find all resources by a deployment and fetch the resource, resourceType, region,
     * resourceProvider, platform, environment, metricValues and metric.
     *
     * @param sessionManager the database session manager
     * @param deploymentId the id of the deployment
     * @return a Single that emits a list of resources
     */
    public Single<List<Resource>> findAllByDeploymentId(SessionManager sessionManager, long deploymentId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery(
            "select distinct r from ResourceDeployment rd " +
                "left join rd.resource r " +
                "left join fetch r.metricValues mv " +
                "left join fetch mv.metric " +
                "left join fetch r.region reg " +
                "left join fetch reg.resourceProvider rp " +
                "left join fetch rp.environment " +
                "left join fetch r.platform p " +
                "left join fetch p.resourceType " +
                "left join fetch r.mainResource mr " +
                "left join fetch mr.metricValues mmv " +
                "left join fetch mmv.metric " +
                "left join fetch mr.region mreg " +
                "left join fetch mreg.resourceProvider mrp " +
                "left join fetch mrp.environment " +
                "left join fetch mr.platform mp " +
                "left join fetch mp.resourceType " +
                "where rd.deployment.deploymentId=:deploymentId", Resource.class)
            .setParameter("deploymentId", deploymentId)
            .getResultList()
        );
    }

    /**
     * Find all resources that are locked by a deployment.
     *
     * @param sessionManager the database session manager
     * @param deploymentId the id of the deployment
     * @return a Single that emits a list of resources
     */
    public Single<List<Resource>> findAllLockedByDeploymentId(SessionManager sessionManager, long deploymentId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("from Resource r " +
                "where r.lockedByDeployment.deploymentId=:deploymentId", Resource.class)
            .setParameter("deploymentId", deploymentId)
            .getResultList()
        );
    }

    /**
     * Find all resources by the resourceIds and resourceTypes.
     *
     * @param sessionManager the database session manager
     * @param resourceIds the list of resource ids
     * @param resourceTypes the list resource types
     * @return a Single that emits a list of resources
     */
    public Single<List<Resource>> findAllByResourceIdsAndResourceTypes(SessionManager sessionManager,
            Set<Long> resourceIds, List<String> resourceTypes) {
        if (resourceIds.isEmpty()) {
            return Single.just(new ArrayList<>());
        }

        Single<List<Resource>> getMainResources = Single.fromCompletionStage(sessionManager.getSession()
            .createQuery(
                "select distinct mr from MainResource mr " +
                    "where mr.resourceId in :resourceIds and " +
                    "mr.platform.resourceType.resourceType in :resourceTypes", entityClass)
            .setParameter("resourceIds", resourceIds)
            .setParameter("resourceTypes", resourceTypes)
            .getResultList()
        );
        Single<List<Resource>> getSubResources = Single.fromCompletionStage(sessionManager.getSession()
            .createQuery(
                "select distinct sr from SubResource sr " +
                    "where sr.resourceId in :resourceIds and " +
                    "sr.mainResource.platform.resourceType.resourceType in :resourceTypes", entityClass)
            .setParameter("resourceIds", resourceIds)
            .setParameter("resourceTypes", resourceTypes)
            .getResultList()
        );
        return Single.zip(getMainResources, getSubResources, (mainResources, subResources) -> {
                ArrayList<Resource> resources = new ArrayList<>();
                resources.addAll(mainResources);
                resources.addAll(subResources);
                return resources;
            }
        );
    }

    /**
     * Find all resources by the resourceIds and fetch the region, resourceProvider, resourceType,
     * platform, environment, metricValues and metric.
     *
     * @param sessionManager the database session manager
     * @param resourceIds the list of resource ids
     * @return a Single that emits a list of resources
     */
    public Single<List<Resource>> findAllByResourceIdsAndFetch(SessionManager sessionManager, List<Long> resourceIds) {
        if (resourceIds.isEmpty()) {
            return Single.just(new ArrayList<>());
        }
        Single<List<Resource>> getMainResources = Single.fromCompletionStage(sessionManager.getSession()
            .createQuery(
                "select distinct mr from MainResource mr " +
                    "left join fetch mr.metricValues mv " +
                    "left join fetch mv.metric " +
                    "left join fetch mr.region reg " +
                    "left join fetch reg.resourceProvider rp " +
                    "left join fetch rp.environment " +
                    "left join fetch mr.platform p " +
                    "left join fetch p.resourceType " +
                    "where mr.resourceId in :resourceIds", Resource.class)
            .setParameter("resourceIds", resourceIds)
            .getResultList()
        );
        Single<List<Resource>> getSubResources = Single.fromCompletionStage(sessionManager.getSession()
            .createQuery(
                "select distinct sr from SubResource sr " +
                    "left join fetch sr.metricValues mv " +
                    "left join fetch mv.metric " +
                    "left join fetch sr.mainResource mr " +
                    "left join fetch mr.region reg " +
                    "left join fetch reg.resourceProvider rp " +
                    "left join fetch rp.environment " +
                    "left join fetch mr.platform p " +
                    "left join fetch p.resourceType " +
                    "left join fetch mr.metricValues mmv " +
                    "left join fetch mmv.metric " +
                    "where sr.resourceId in :resourceIds", Resource.class)
            .setParameter("resourceIds", resourceIds)
            .getResultList()
        );
        return Single.zip(getMainResources, getSubResources, (mainResources, subResources) -> {
                ArrayList<Resource> resources = new ArrayList<>();
                resources.addAll(mainResources);
                resources.addAll(subResources);
                return resources;
            }
        );
    }

    /**
     * Find all sub resources for a resource.
     *
     * @param sessionManager the database session manager
     * @param resourceId the id of the resource
     * @return a Single that emits a list of all found sub resources
     */
    public Single<List<SubResource>> findAllSubresources(SessionManager sessionManager, long resourceId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct sr from SubResource  sr " +
                "left join fetch sr.metricValues mv " +
                "left join fetch mv.metric m " +
                "where sr.mainResource.resourceId=:resourceId", SubResource.class)
            .setParameter("resourceId", resourceId)
            .getResultList()
        );
    }

    /**
     * Find a main resource with the platform k8s and name.
     *
     * @param sessionManager the database session
     * @param name the name of the resource
     * @return a Maybe that emits the resource if it exists, else null
     */
    public Maybe<MainResource> findClusterByName(SessionManager sessionManager, String name) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct mr from MainResource mr " +
                "left join fetch mr.metricValues mmv " +
                "left join fetch mmv.metric " +
                "left join fetch mr.subResources sr " +
                "left join fetch sr.metricValues smv " +
                "left join fetch smv.metric " +
                "where mr.name=:name and mr.platform.platform=:platform", MainResource.class)
            .setParameter("name", name)
            .setParameter("platform", PlatformEnum.K8S.getValue())
            .getSingleResultOrNull()
            .thenApply(mainResource -> {
                if (mainResource != null) {
                    mainResource.setSubResources(mainResource.getSubResources().stream().distinct()
                        .collect(Collectors.toList()));
                }
                return mainResource;
            })
        );
    }

    /**
     * Find all function deployment scrape targets.
     *
     * @param sessionManager the database session
     * @return a Maybe that emits the resource if it exists, else null
     */
    public Single<Set<FindAllFunctionDeploymentScrapeTargetsDTO>> findAllFunctionDeploymentTargets(
        SessionManager sessionManager) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct " +
                    "new at.uibk.dps.rm.entity.dto.resource.FindAllFunctionDeploymentScrapeTargetsDTO(" +
                    "fd.resourceDeploymentId, fd.deployment.deploymentId, fd.resource.resourceId, fd.baseUrl, " +
                    "fd.metricsPort) from FunctionDeployment fd " +
                    "where fd.status.statusValue=:statusDeployed and fd.resource.platform.platform=:platformEc2",
                FindAllFunctionDeploymentScrapeTargetsDTO.class)
            .setParameter("statusDeployed", DeploymentStatusValue.DEPLOYED.getValue())
            .setParameter("platformEc2", PlatformEnum.EC2.getValue())
            .getResultList()
            .thenApply(Set::copyOf)
        );
    }

    /**
     * Find all OpenFaaS scrape targets.
     *
     * @param sessionManager the database session
     * @return a Maybe that emits the resource if it exists, else null
     */
    public Single<List<FindAllOpenFaaSScrapeTargetsDTO>> findAllOpenFaaSTargets(SessionManager sessionManager) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct new at.uibk.dps.rm.entity.dto.resource.FindAllOpenFaaSScrapeTargetsDTO(" +
                    "r.resourceId, " +
                    "(select mv.valueString from MetricValue mv where mv.metric.metric='base-url' " +
                    "and mv.resource.resourceId=r.resourceId), " +
                    "(select mv.valueNumber from MetricValue mv where mv.metric.metric='metrics-port' " +
                    "and mv.resource.resourceId=r.resourceId) " +
                    ") from Resource r " +
                    "left join r.metricValues mv " +
                    "where r.platform.platform=:platformOpenFaaS",
                FindAllOpenFaaSScrapeTargetsDTO.class)
            .setParameter("platformOpenFaaS", PlatformEnum.OPENFAAS.getValue())
            .getResultList()
        );
    }

    private Single<List<Resource>> mergeMainAndSubResources(SessionManager sessionManager, String mainQuery,
            String subQuery) {
        Single<List<Resource>> getMainResources = Single.fromCompletionStage(sessionManager.getSession()
            .createQuery(mainQuery, entityClass).getResultList()
        );
        Single<List<Resource>> getSubResources = Single.fromCompletionStage(sessionManager.getSession()
            .createQuery(subQuery, entityClass).getResultList());
        return Single.zip(getMainResources, getSubResources, (mainResources, subResources) -> {
                ArrayList<Resource> resources = new ArrayList<>();
                resources.addAll(mainResources);
                resources.addAll(subResources);
                return resources;
            }
        );
    }
}
