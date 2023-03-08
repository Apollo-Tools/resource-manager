package at.uibk.dps.rm.service;

import at.uibk.dps.rm.service.rxjava3.database.log.ReservationLogService;
import at.uibk.dps.rm.service.rxjava3.database.metric.ResourceTypeMetricService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationStatusService;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountCredentialsService;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountService;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionResourceService;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionService;
import at.uibk.dps.rm.service.rxjava3.database.function.RuntimeService;
import at.uibk.dps.rm.service.rxjava3.database.log.LogService;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.RegionService;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.ResourceProviderService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ReservationService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricTypeService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricValueService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceTypeService;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.VPCService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentService;
import at.uibk.dps.rm.service.rxjava3.util.FilePathService;
import io.vertx.rxjava3.core.Vertx;
import lombok.Getter;

@Getter
public class ServiceProxyProvider {
    private final AccountService accountService;
    private final AccountCredentialsService accountCredentialsService;
    private final CredentialsService credentialsService;
    private final FunctionService functionService;
    private final FunctionResourceService functionResourceService;
    private final LogService logService;
    private final MetricService metricService;
    private final MetricTypeService metricTypeService;
    private final MetricValueService metricValueService;
    private final RegionService regionService;
    private final ReservationService reservationService;
    private final ReservationLogService reservationLogService;
    private final ResourceService resourceService;
    private final ResourceProviderService resourceProviderService;
    private final ResourceReservationService resourceReservationService;
    private final ResourceReservationStatusService resourceReservationStatusService;
    private final ResourceTypeService resourceTypeService;
    private final ResourceTypeMetricService resourceTypeMetricService;
    private final RuntimeService runtimeService;
    private final VPCService vpcService;
    private final DeploymentService deploymentService;
    private final FilePathService filePathService;

    public ServiceProxyProvider(Vertx vertx) {
        accountService = AccountService.createProxy(vertx, "account-service-address");
        accountCredentialsService = AccountCredentialsService
            .createProxy(vertx, "account-credentials-service-address");
        credentialsService = CredentialsService.createProxy(vertx, "credentials-service-address");
        functionService = FunctionService.createProxy(vertx, "function-service-address");
        functionResourceService = FunctionResourceService.createProxy(
            vertx, "function-resource-service-address");
        logService = LogService.createProxy(vertx, "log-service-address");
        metricService = MetricService.createProxy(vertx,"metric-service-address");
        metricTypeService = MetricTypeService.createProxy(vertx, "metric-type-service-address");
        metricValueService = MetricValueService.createProxy(vertx,"metric-value-service-address");
        regionService = RegionService.createProxy(vertx, "region-service-address");
        reservationService = ReservationService.createProxy(vertx, "reservation-service-address");
        reservationLogService = ReservationLogService.createProxy(vertx, "reservation-log-service-address");
        resourceService = ResourceService.createProxy(vertx,"resource-service-address");
        resourceProviderService = ResourceProviderService.createProxy(
            vertx, "resource-provider-service-address");
        resourceReservationService = ResourceReservationService
            .createProxy(vertx, "resource-reservation-service-address");
        resourceReservationStatusService = ResourceReservationStatusService
            .createProxy(vertx, "resource-reservation-status-service-address");
        resourceTypeService = ResourceTypeService.createProxy(vertx,"resource-type-service-address");
        resourceTypeMetricService = ResourceTypeMetricService
            .createProxy(vertx, "resource-type-metric-service-address");
        runtimeService = RuntimeService.createProxy(vertx, "runtime-service-address");
        vpcService = VPCService.createProxy(vertx, "vpc-service-address");
        deploymentService = DeploymentService.createProxy(vertx, "deployment-service-address");
        filePathService = FilePathService.createProxy(vertx, "file-path-service-address");
    }
}
