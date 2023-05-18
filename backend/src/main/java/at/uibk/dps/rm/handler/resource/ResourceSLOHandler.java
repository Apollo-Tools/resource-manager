package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.ensemble.ResourceEnsembleStatus;
import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.handler.metric.MetricChecker;
import at.uibk.dps.rm.handler.metric.MetricValueChecker;
import at.uibk.dps.rm.util.validation.SLOCompareUtility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Processes the http requests that concern filtering resources by Service Level Objectives.
 *
 * @author matthi-g
 */
public class ResourceSLOHandler {

    private final ResourceChecker resourceChecker;

    private final MetricChecker metricChecker;

    private final MetricValueChecker metricValueChecker;

    /**
     * Create an instance from the resourceChecker, metricChecker and metricValueChecker.
     *
     * @param resourceChecker the resource checker
     * @param metricChecker the metric checker
     * @param metricValueChecker the metric value checker
     */
    public ResourceSLOHandler(ResourceChecker resourceChecker, MetricChecker metricChecker,
            MetricValueChecker metricValueChecker) {
        this.resourceChecker = resourceChecker;
        this.metricChecker = metricChecker;
        this.metricValueChecker = metricValueChecker;
    }

    /**
     * Validate the resources for a create ensemble request.
     *
     * @param rc the rounting context
     * @param requestDTO the request
     */
    public void validateNewResourceEnsembleSLOs(RoutingContext rc, CreateEnsembleRequest requestDTO) {
        getResourcesBySLOs(requestDTO)
            .flatMapCompletable(resources -> {
                List<ResourceId> resourceIds = requestDTO.getResources();
                return checkResourcesFulfillSLOs(resourceIds, resources);
            })
            .subscribe(rc::next, throwable -> rc.fail(400, throwable));
    }

    /**
     * Validate the resources from an existing ensemble.
     *
     * @param ensemble the ensemble
     * @return a Single that emits the List of pairs of resource_ids and their validation status
     */
    public Single<List<ResourceEnsembleStatus>> validateExistingEnsemble(GetOneEnsemble ensemble) {
        return getResourcesBySLOs(ensemble)
            .flatMap(resources -> Observable.fromIterable(ensemble.getResources())
                .map(resource -> {
                    ResourceId resourceId = new ResourceId();
                    resourceId.setResourceId(resource.getResourceId());
                    return resourceId;
                })
                .toList()
                .flatMap(resourceIds -> getResourceEnsembleStatus(resourceIds, resources)));
    }

    /**
     * Find and return all resources that fulfill the Service Level Objectives defined in the
     * sloRequest
     *
     * @param sloRequest the servie level objective request
     * @return a Single that emits the list of found resources as JsonArray
     */
    public Single<JsonArray> getResourcesBySLOs(SLORequest sloRequest) {
        List<ServiceLevelObjective> serviceLevelObjectives = sloRequest.getServiceLevelObjectives();
        List<Completable> completables = new ArrayList<>();
        serviceLevelObjectives.forEach(slo -> completables.add(metricChecker.checkServiceLevelObjectives(slo)));
        return Completable.merge(completables)
            .andThen(Observable.fromIterable(serviceLevelObjectives)
                .map(ServiceLevelObjective::getName)
                .toList()
                .flatMap(metrics -> resourceChecker.checkFindAllBySLOs(metrics, sloRequest.getRegions(),
                        sloRequest.getProviders(), sloRequest.getResourceTypes())))
            .flatMap(this::mapMetricValuesToResources)
            .map(this::mapJsonListToResourceList)
            .map(resources -> filterAndSortResultList(resources, serviceLevelObjectives));
    }

    /**
     * Get the slo status of all resources
     *
     * @param validResourceIds the ids of all valid resources
     * @param resources the resources to validate
     * @return the List of pairs of resource_ids and their validation status being true for valid
     * and false for invalid
     * @throws JsonProcessingException  when an error occurs during json processing
     */
    public Single<List<ResourceEnsembleStatus>> getResourceEnsembleStatus(List<ResourceId> validResourceIds,
            JsonArray resources) throws JsonProcessingException {
        ObjectMapper mapper = DatabindCodec.mapper();
        List<Resource> resourceList = mapper.readValue(resources.toString(), new TypeReference<>() {});
        return Observable.fromIterable(validResourceIds)
            .map(ResourceId::getResourceId)
            .map(resourceId -> {
                boolean isValid = resourceList.stream()
                    .anyMatch(resource -> Objects.equals(resource.getResourceId(), resourceId));
                return new ResourceEnsembleStatus(resourceId, isValid);
            })
            .toList();
    }

    /**
     * Check if resources fulfill service level objectives.
     *
     * @param validResourceIds the ids of all valid resources
     * @param resources the resources to validate
     * @return a Completable if all resources fulfill the slos else a throwable is thrown
     * @throws JsonProcessingException when an error occurs during json processing
     */
    public Completable checkResourcesFulfillSLOs(List<ResourceId> validResourceIds, JsonArray resources)
        throws JsonProcessingException {
        ObjectMapper mapper = DatabindCodec.mapper();
        List<Resource> resourceList = mapper.readValue(resources.toString(), new TypeReference<>() {});
        return Observable.fromIterable(validResourceIds)
            .map(ResourceId::getResourceId)
            .all(resourceId -> resourceList.stream()
                .anyMatch(resource -> Objects.equals(resource.getResourceId(), resourceId)))
            .map(result -> {
                if (!result) {
                    throw new Throwable("slo mismatch");
                }
                return true;
            })
            .ignoreElement();
    }

    /**
     * Map metric values all resources.
     *
     * @param resources the resources
     * @return the list of resources including the corresponding metric values
     */
    protected Single<List<JsonObject>> mapMetricValuesToResources(JsonArray resources) {
        //noinspection unchecked
        return Observable
            .fromIterable((List<JsonObject>) resources.getList())
            .flatMapSingle(this::findMetricValuesForResource)
            .toList();
    }

    /**
     * Find all metric values for a resource and map them to it.
     *
     * @param jsonResource the resource
     * @return the resource including the corresponding metric values
     */
    private Single<JsonObject> findMetricValuesForResource(JsonObject jsonResource) {
        //noinspection unchecked
        return Observable
            .fromIterable((List<JsonObject>) jsonResource
                .getJsonArray("metric_values")
                .getList())
            .flatMapSingle(metricValue -> metricValueChecker.checkFindOne(metricValue.getLong("metric_value_id")))
            .toList()
            .map(metrics -> {
                jsonResource.put("metric_values", metrics);
                return jsonResource;
            });
    }

    /**
     * Map a list of resources in JSON-format to a list of resource objects.
     *
     * @param jsonObjectList the list of resources
     * @return the transformed list of resources
     */
    private List<Resource> mapJsonListToResourceList(List<JsonObject> jsonObjectList) {
        List<Resource> resourceList = new ArrayList<>();
        for (JsonObject resource : jsonObjectList) {
            resourceList.add(resource.mapTo(Resource.class));
        }
        return resourceList;
    }

    /**
     * Filter and sort the resources based on the service Level Objectives.
     *
     * @param resources the resources
     * @param serviceLevelObjectives the service level objectives used for filtering and sorting
     * @return the filtered and sorted resources as JsonArray
     */
    protected JsonArray filterAndSortResultList(List<Resource> resources,
                                                List<ServiceLevelObjective> serviceLevelObjectives) {
        List<JsonObject> filteredAndSorted = resources
            .stream()
            .filter(resource -> SLOCompareUtility.resourceFilterBySLOValueType(resource, serviceLevelObjectives))
            .sorted((r1, r2) -> sortResourceBySLO(r1, r2, serviceLevelObjectives))
            .map(JsonObject::mapFrom)
            .collect(Collectors.toList());

        return new JsonArray(filteredAndSorted);
    }

    /**
     * The sorting condition for the resources based on the serviceLevelObjectives
     *
     * @param r1 the first resource to compare
     * @param r2 the second resource to compare
     * @param serviceLevelObjectives the service level objectives
     * @return a positive value if r1 should be ranked higher than r2 else a negative value
     */
    protected int sortResourceBySLO(Resource r1, Resource r2, List<ServiceLevelObjective> serviceLevelObjectives) {
        for (int i = 0; i < serviceLevelObjectives.size(); i++) {
            ServiceLevelObjective slo = serviceLevelObjectives.get(i);
            if (slo.getValue().get(0).getSloValueType() != SLOValueType.NUMBER) {
                continue;
            }
            for (MetricValue metricValue1 : r1.getMetricValues()) {
                Metric metric1 = metricValue1.getMetric();
                if (metric1.getMetric().equals(slo.getName())) {
                    for (MetricValue metricValue2 : r2.getMetricValues()) {
                        Metric metric2 = metricValue2.getMetric();
                        if (metric2.getMetric().equals(slo.getName())) {
                            int compareValue = ExpressionType.compareValues(slo.getExpression(),
                                metricValue1.getValueNumber().doubleValue(),
                                metricValue2.getValueNumber().doubleValue());
                            if (compareValue != 0 || i == serviceLevelObjectives.size() - 1) {
                                return compareValue;
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }
}
