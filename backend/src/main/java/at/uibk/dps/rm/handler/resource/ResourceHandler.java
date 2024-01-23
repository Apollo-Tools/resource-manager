package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.resource.FindResourceBySloDTO;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.monitoring.MonitoringMetricEnum;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricquery.MetricQueryService;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.misc.HttpHelper;
import at.uibk.dps.rm.util.misc.MetricValueMapper;
import at.uibk.dps.rm.util.monitoring.MetricQueryProvider;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Processes the http requests that concern the resource entity.
 *
 * @author matthi-g
 */
public class ResourceHandler extends ValidationHandler {

    private final ResourceService resourceService;

    private final MetricQueryService metricQueryService;

    /**
     * Create an instance from the resourceService.
     *
     * @param resourceService the service
     */
    public ResourceHandler(ResourceService resourceService, MetricQueryService metricQueryService) {
        super(resourceService);
        this.resourceService = resourceService;
        this.metricQueryService = metricQueryService;
    }

    /**
     * Find all sub resources of a main resource.
     *
     * @param rc the RoutingContext of the request
     * @return a Single that emits all found sub resources as JsonArray
     */
    public Single<JsonArray> getAllSubResourcesByMainResource(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(resourceService::findAllSubResources);
    }

    /**
     * Find and return all resources that fulfill the service level objectives from the request.
     *
     * @param rc the RoutingContext of the request
     * @return a Single that emits the found entities as JsonArray
     */
    public Single<JsonArray> getAllBySLOs(RoutingContext rc) {
        return resourceService.findAllBySLOs(rc.body().asJsonObject());
    }

    public Single<JsonArray> getAllByNonMonitoredSLOs(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        SLORequest sloRequest = requestBody.mapTo(SLORequest.class);
        return new ConfigUtility(Vertx.currentContext().owner()).getConfigDTO()
            .flatMap(configDTO -> resourceService.findAllByNonMonitoredSLOs(requestBody)
                .flatMap(resources -> Observable.fromIterable(resources)
                    .map(resource -> ((JsonObject) resource))
                    .map(resource -> resource.mapTo(Resource.class))
                    .collect(Collectors.toMap(resource -> resource.getResourceId().toString(), resource -> resource))
                    .flatMapObservable(filteredResources -> {
                        Set<String> resourceIds = filteredResources.keySet();
                        HashSetValuedHashMap<String, String> regionResources = new HashSetValuedHashMap<>();
                        HashSetValuedHashMap<Pair<String, String>, String> platformResources =
                            new HashSetValuedHashMap<>();
                        HashSetValuedHashMap<String, String> instanceTypeResources = new HashSetValuedHashMap<>();
                        filteredResources.values().forEach(resource -> {
                            Map<String, MetricValue> metricValues =
                                MetricValueMapper.mapMetricValues(resource.getMetricValues());
                            if (metricValues.containsKey("instance-type")) {
                                instanceTypeResources.put(metricValues.get("instance-type").getValueString(),
                                    resource.getResourceId().toString());
                            }
                            if (resource instanceof SubResourceDTO) {
                                SubResourceDTO subResourceDTO = (SubResourceDTO) resource;
                                regionResources.put(subResourceDTO.getRegion().getRegionId().toString(),
                                    subResourceDTO.getResourceId().toString());
                                Platform platform = subResourceDTO.getPlatform();
                                platformResources.put(new ImmutablePair<>(platform.getPlatformId().toString(),
                                        platform.getPlatform()), subResourceDTO.getResourceId().toString());
                                if (subResourceDTO.getPlatform().getPlatform().equals(PlatformEnum.LAMBDA.getValue())) {
                                    instanceTypeResources.put(PlatformEnum.LAMBDA.getValue(),
                                        subResourceDTO.getResourceId().toString());
                                }
                            } else {
                                regionResources.put(resource.getMain().getRegion().getRegionId().toString(),
                                    resource.getResourceId().toString());
                                Platform platform = resource.getMain().getPlatform();
                                platformResources.put(new ImmutablePair<>(platform.getPlatformId().toString(),
                                        platform.getPlatform()), resource.getResourceId().toString());
                                if (resource.getMain().getPlatform().getPlatform().equals(PlatformEnum.LAMBDA.getValue())) {
                                    instanceTypeResources.put(PlatformEnum.LAMBDA.getValue(),
                                        resource.getResourceId().toString());
                                }
                            }
                        });
                        MetricQueryProvider queryProvider = new MetricQueryProvider(metricQueryService);
                        return Observable.fromIterable(sloRequest.getServiceLevelObjectives())
                            .filter(slo -> MonitoringMetricEnum.fromSLO(slo) != null)
                            .flatMapSingle(slo -> {
                                // TODO: fix deployment
                                MonitoringMetricEnum metric = MonitoringMetricEnum.fromSLO(slo);
                                return queryProvider.getMetricQuery(configDTO, metric, slo, filteredResources,
                                    resourceIds, regionResources, platformResources, instanceTypeResources);
                            });
                    })
                    .reduce((currSet, nextSet) -> {
                        currSet.retainAll(nextSet);
                        return currSet;
                    })
                    .switchIfEmpty(Single.just(Set.of()))
                    .flatMapObservable(Observable::fromIterable)
                    .map(resource -> {
                        FindResourceBySloDTO result;
                        if (resource instanceof SubResourceDTO) {
                            result = new FindResourceBySloDTO((SubResourceDTO) resource);
                        } else if (resource instanceof MainResource) {
                            result = new FindResourceBySloDTO((MainResource) resource);
                        } else {
                            result = new FindResourceBySloDTO((SubResource) resource);
                        }
                        return result;
                    })
                    .map(JsonObject::mapFrom)
                    .toList()
                    .map(JsonArray::new)
                )
            );
    }

    /**
     * Find and return all resources that fulfill the service level objectives from the request.
     *
     * @param rc the RoutingContext of the request
     * @return a Single that emits the found entities as JsonArray
     */
    public Single<JsonArray> getAllLockedByDeployment(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(resourceService::findAllLockedByDeployment);
    }
}
