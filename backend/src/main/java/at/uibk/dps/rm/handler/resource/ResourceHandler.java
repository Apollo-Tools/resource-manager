package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.resource.FindResourceBySloDTO;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricquery.MetricQueryService;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.misc.HttpHelper;
import at.uibk.dps.rm.util.validation.SLOValidator;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the resource entity.
 *
 * @author matthi-g
 */
public class ResourceHandler extends ValidationHandler {

    private final ResourceService resourceService;

    private final MetricService metricService;

    private final MetricQueryService metricQueryService;

    /**
     * Create an instance from the resourceService.
     *
     * @param resourceService the service
     */
    public ResourceHandler(ResourceService resourceService, MetricService metricService,
            MetricQueryService metricQueryService) {
        super(resourceService);
        this.resourceService = resourceService;
        this.metricService = metricService;
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
     * Find all resources that match a list of service level objectives.
     *
     * @param rc the RoutingContext of the request
     * @return a Single that emits all found resources as JsonArray that match the specified
     * service level objectives
     */
    public Single<JsonArray> getAllBySLOs(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        SLORequest sloRequest = requestBody.mapTo(SLORequest.class);
        return new ConfigUtility(Vertx.currentContext().owner()).getConfigDTO()
            .flatMap(configDTO -> metricService.checkMetricTypeForSLOs(requestBody)
                .andThen(resourceService.findAllByNonMonitoredSLOs(requestBody))
                .flatMap(resources -> {
                    SLOValidator sloValidator = new SLOValidator(metricQueryService, sloRequest, configDTO);
                    return sloValidator.filterResourcesByMonitoredMetrics(resources)
                        .flatMapObservable(Observable::fromIterable)
                        .sorted((resource1, resource2) -> sloValidator.sortResourceBySLOs(resource1, resource2,
                            sloRequest.getServiceLevelObjectives()))
                        .map(resource -> {
                            FindResourceBySloDTO result;
                            if (resource instanceof SubResourceDTO) {
                                result = new FindResourceBySloDTO((SubResourceDTO) resource,
                                    sloRequest.getServiceLevelObjectives());
                            } else if (resource instanceof MainResource) {
                                result = new FindResourceBySloDTO((MainResource) resource,
                                    sloRequest.getServiceLevelObjectives());
                            } else {
                                result = new FindResourceBySloDTO(new SubResourceDTO((SubResource) resource),
                                    sloRequest.getServiceLevelObjectives());
                            }
                            return result;
                        })
                        .map(JsonObject::mapFrom)
                        .toList()
                        .map(JsonArray::new);
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
