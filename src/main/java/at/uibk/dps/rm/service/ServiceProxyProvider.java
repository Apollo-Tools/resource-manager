package at.uibk.dps.rm.service;

import at.uibk.dps.rm.service.rxjava3.database.metric.MetricTypeService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricValueService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceTypeService;
import io.vertx.rxjava3.core.Vertx;

public class ServiceProxyProvider {
    private final ResourceService resourceService;
    private final ResourceTypeService resourceTypeService;
    private final MetricService metricService;
    private final MetricValueService metricValueService;

    private final MetricTypeService metricTypeService;

    public ServiceProxyProvider(Vertx vertx) {
        resourceService = ResourceService.createProxy(vertx,"resource-service-address");
        resourceTypeService = ResourceTypeService.createProxy(vertx,"resource-type-service-address");
        metricService = MetricService.createProxy(vertx,"metric-service-address");
        metricValueService = MetricValueService.createProxy(vertx,"metric-value-service-address");
        metricTypeService = MetricTypeService.createProxy(vertx, "metric-type-service-address");
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

    public MetricTypeService getMetricTypeService() {
        return metricTypeService;
    }
}
