package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.monitoring.MonitoringMetricEnum;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricquery.MetricQueryService;
import at.uibk.dps.rm.util.misc.HttpHelper;
import at.uibk.dps.rm.util.monitoring.MetricQueryProvider;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.Objects;
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
        return resourceService.findAllByNonMonitoredSLOs(requestBody)
            .flatMap(resources -> Observable.fromIterable(resources)
                .map(resource -> ((JsonObject) resource))
                .map(resource -> resource.getLong("resource_id").toString())
                .collect(Collectors.toSet())
                .flatMapObservable(resourceIds -> Observable.fromIterable(sloRequest.getServiceLevelObjectives())
                    .filter(slo -> MonitoringMetricEnum.fromSLO(slo) != null)
                    .flatMapSingle(slo -> {
                        MonitoringMetricEnum metric = MonitoringMetricEnum.fromSLO(slo);
                        return Observable.fromIterable(Objects.requireNonNull(MetricQueryProvider
                                .getMetricQuery(metric, slo, resourceIds)))
                            .flatMapSingle(query -> metricQueryService.collectInstantMetric(query.toString()))
                            .flatMapSingle(vmResults -> Observable.fromIterable(vmResults)
                                .map(vmResult -> vmResult.getMetric().get("resource"))
                                .collect(Collectors.toSet()))
                            .reduce((currSet, nextSet) -> {
                                currSet.addAll(nextSet);
                                return currSet;
                            })
                            .switchIfEmpty(Single.just(Set.of()));
                    }))
                .reduce((currSet, nextSet) -> {
                    currSet.retainAll(nextSet);
                    return currSet;
                })
                .switchIfEmpty(Single.just(Set.of()))
                .map(result -> {
                    System.out.println(result);
                    return resources;
                })
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
