package at.uibk.dps.rm.service;

import at.uibk.dps.rm.service.rxjava3.metric.MetricService;
import at.uibk.dps.rm.service.rxjava3.metric.MetricValueService;
import at.uibk.dps.rm.service.rxjava3.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.resource.ResourceTypeService;
import io.vertx.rxjava3.core.Vertx;

public class ServiceProxyProvider {
    private final ResourceService resourceService;
    private final ResourceTypeService resourceTypeService;
    private final MetricService metricService;
    private final MetricValueService metricValueService;

    public ServiceProxyProvider(Vertx vertx) {
        resourceService = ResourceService.createProxy(vertx,"resource-service-address");
        resourceTypeService = ResourceTypeService.createProxy(vertx,"resource-type-service-address");
        metricService = MetricService.createProxy(vertx,"metric-service-address");
        metricValueService = MetricValueService.createProxy(vertx,"metric-value-service-address");
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    public ResourceTypeService getResourceTypeService() {
        return resourceTypeService;
    }

    public MetricService getMetricService() {
        return metricService;
    }

    public MetricValueService getMetricValueService() {
        return metricValueService;
    }
}
