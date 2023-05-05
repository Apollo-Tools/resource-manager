package at.uibk.dps.rm.service;

import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleSLOService;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleService;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.ResourceEnsembleService;
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
import at.uibk.dps.rm.service.rxjava3.database.service.ServiceService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentService;
import at.uibk.dps.rm.service.rxjava3.util.FilePathService;
import at.uibk.dps.rm.verticle.DatabaseVerticle;
import io.vertx.rxjava3.core.Vertx;
import lombok.Getter;

/**
 * This class is used as a provider of the client side of all service proxy services that are
 * available at the resource manager. To use the service clients their address has to get bound
 * to a ServiceBinder first like in {@link DatabaseVerticle}.
 *
 * @author matthi-g
 */
@Getter
public class ServiceProxyProvider {
    private final AccountService accountService;
    private final AccountCredentialsService accountCredentialsService;
    private final CredentialsService credentialsService;
    private final EnsembleService ensembleService;
    private final EnsembleSLOService ensembleSLOService;
    private final FunctionService functionService;
    private final FunctionResourceService functionResourceService;
    private final LogService logService;
    private final MetricService metricService;
    private final MetricTypeService metricTypeService;
    private final MetricValueService metricValueService;
    private final RegionService regionService;
    private final ReservationService reservationService;
    private final ReservationLogService reservationLogService;
    private final ResourceEnsembleService resourceEnsembleService;
    private final ResourceService resourceService;
    private final ResourceProviderService resourceProviderService;
    private final ResourceReservationService resourceReservationService;
    @SuppressWarnings("PMD.LongVariable")
    private final ResourceReservationStatusService resourceReservationStatusService;
    private final ResourceTypeService resourceTypeService;
    private final ResourceTypeMetricService resourceTypeMetricService;
    private final RuntimeService runtimeService;
    private final ServiceService serviceService;
    private final VPCService vpcService;
    private final DeploymentService deploymentService;
    private final FilePathService filePathService;

    /**
     * Create an instance from vertx. All client proxies get setup in this method.
     *
     * @param vertx the current vertx instance
     */
    public ServiceProxyProvider(Vertx vertx) {
        accountService = AccountService.createProxy(vertx);
        accountCredentialsService = AccountCredentialsService.createProxy(vertx);
        credentialsService = CredentialsService.createProxy(vertx);
        ensembleService = EnsembleService.createProxy(vertx);
        ensembleSLOService = EnsembleSLOService.createProxy(vertx);
        functionService = FunctionService.createProxy(vertx);
        functionResourceService = FunctionResourceService.createProxy(vertx);
        logService = LogService.createProxy(vertx);
        metricService = MetricService.createProxy(vertx);
        metricTypeService = MetricTypeService.createProxy(vertx);
        metricValueService = MetricValueService.createProxy(vertx);
        regionService = RegionService.createProxy(vertx);
        reservationService = ReservationService.createProxy(vertx);
        reservationLogService = ReservationLogService.createProxy(vertx);
        resourceEnsembleService = ResourceEnsembleService.createProxy(vertx);
        resourceService = ResourceService.createProxy(vertx);
        resourceProviderService = ResourceProviderService.createProxy(vertx);
        resourceReservationService = ResourceReservationService.createProxy(vertx);
        resourceReservationStatusService = ResourceReservationStatusService.createProxy(vertx);
        resourceTypeService = ResourceTypeService.createProxy(vertx);
        resourceTypeMetricService = ResourceTypeMetricService.createProxy(vertx);
        runtimeService = RuntimeService.createProxy(vertx);
        serviceService = ServiceService.createProxy(vertx);
        vpcService = VPCService.createProxy(vertx);
        deploymentService = DeploymentService.createProxy(vertx);
        filePathService = FilePathService.createProxy(vertx);
    }
}
