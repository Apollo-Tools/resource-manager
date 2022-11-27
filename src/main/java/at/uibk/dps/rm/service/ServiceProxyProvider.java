package at.uibk.dps.rm.service;

import at.uibk.dps.rm.service.rxjava3.database.reservation.ReservationService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricTypeService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricValueService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceTypeService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentService;
import io.vertx.rxjava3.core.Vertx;
import lombok.Getter;

@Getter
public class ServiceProxyProvider {
    private final ResourceService resourceService;
    private final ResourceTypeService resourceTypeService;
    private final MetricService metricService;
    private final MetricValueService metricValueService;
    private final MetricTypeService metricTypeService;
    private final ReservationService reservationService;
    private final ResourceReservationService resourceReservationService;
    private final DeploymentService deploymentService;

    public ServiceProxyProvider(Vertx vertx) {
        resourceService = ResourceService.createProxy(vertx,"resource-service-address");
        resourceTypeService = ResourceTypeService.createProxy(vertx,"resource-type-service-address");
        metricService = MetricService.createProxy(vertx,"metric-service-address");
        metricValueService = MetricValueService.createProxy(vertx,"metric-value-service-address");
        metricTypeService = MetricTypeService.createProxy(vertx, "metric-type-service-address");
        reservationService = ReservationService.createProxy(vertx, "reservation-service-address");
        resourceReservationService = ResourceReservationService
                .createProxy(vertx, "resource-reservation-service-address");
        deploymentService = DeploymentService.createProxy(vertx, "deployment-service-address");
    }
}
