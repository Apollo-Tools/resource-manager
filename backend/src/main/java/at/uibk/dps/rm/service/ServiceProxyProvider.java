package at.uibk.dps.rm.service;

import at.uibk.dps.rm.service.rxjava3.database.account.AccountCredentialsService;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountService;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionResourceService;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionService;
import at.uibk.dps.rm.service.rxjava3.database.function.RuntimeService;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.RegionService;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.ResourceProviderService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ReservationService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricTypeService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricValueService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceTypeService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentService;
import at.uibk.dps.rm.service.rxjava3.util.FilePathService;
import io.vertx.rxjava3.core.Vertx;
import lombok.Getter;

@Getter
public class ServiceProxyProvider {
    private final AccountCredentialsService accountCredentialsService;
    private final AccountService accountService;
    private final ResourceProviderService resourceProviderService;
    private final RegionService regionService;
    private final CredentialsService credentialsService;
    private final ResourceService resourceService;
    private final ResourceTypeService resourceTypeService;
    private final MetricService metricService;
    private final MetricValueService metricValueService;
    private final MetricTypeService metricTypeService;
    private final RuntimeService runtimeService;
    private final FunctionService functionService;
    private final FunctionResourceService functionResourceService;
    private final ReservationService reservationService;
    private final ResourceReservationService resourceReservationService;
    private final DeploymentService deploymentService;

    private final FilePathService filePathService;

    public ServiceProxyProvider(Vertx vertx) {
        accountCredentialsService = AccountCredentialsService
            .createProxy(vertx, "account-credentials-service-address");
        accountService = AccountService.createProxy(vertx, "account-service-address");
        resourceProviderService = ResourceProviderService.createProxy(
            vertx, "resource-provider-service-address");
        regionService = RegionService.createProxy(vertx, "region-service-address");
        credentialsService = CredentialsService.createProxy(vertx, "credentials-service-address");
        resourceService = ResourceService.createProxy(vertx,"resource-service-address");
        resourceTypeService = ResourceTypeService.createProxy(vertx,"resource-type-service-address");
        metricService = MetricService.createProxy(vertx,"metric-service-address");
        metricValueService = MetricValueService.createProxy(vertx,"metric-value-service-address");
        metricTypeService = MetricTypeService.createProxy(vertx, "metric-type-service-address");
        runtimeService = RuntimeService.createProxy(vertx, "runtime-service-address");
        functionService = FunctionService.createProxy(vertx, "function-service-address");
        functionResourceService = FunctionResourceService.createProxy(
            vertx, "function-resource-service-address");
        reservationService = ReservationService.createProxy(vertx, "reservation-service-address");
        resourceReservationService = ResourceReservationService
                .createProxy(vertx, "resource-reservation-service-address");
        deploymentService = DeploymentService.createProxy(vertx, "deployment-service-address");
        filePathService = FilePathService.createProxy(vertx, "file-path-service-address");
    }
}
