package at.uibk.dps.rm.service;

import at.uibk.dps.rm.rx.service.rxjava3.database.artifact.FunctionTypeService;
import at.uibk.dps.rm.rx.service.rxjava3.database.artifact.ServiceTypeService;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleService;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.ResourceEnsembleService;
import at.uibk.dps.rm.rx.service.rxjava3.database.log.DeploymentLogService;
import at.uibk.dps.rm.rx.service.rxjava3.database.metric.PlatformMetricService;
import at.uibk.dps.rm.service.rxjava3.database.deployment.*;
import at.uibk.dps.rm.rx.service.rxjava3.database.account.AccountNamespaceService;
import at.uibk.dps.rm.rx.service.rxjava3.database.account.AccountService;
import at.uibk.dps.rm.rx.service.rxjava3.database.account.CredentialsService;
import at.uibk.dps.rm.rx.service.rxjava3.database.account.NamespaceService;
import at.uibk.dps.rm.rx.service.rxjava3.database.function.FunctionService;
import at.uibk.dps.rm.rx.service.rxjava3.database.function.RuntimeService;
import at.uibk.dps.rm.rx.service.rxjava3.database.log.LogService;
import at.uibk.dps.rm.rx.service.rxjava3.database.resource.PlatformService;
import at.uibk.dps.rm.rx.service.rxjava3.database.resourceprovider.EnvironmentService;
import at.uibk.dps.rm.rx.service.rxjava3.database.resourceprovider.RegionService;
import at.uibk.dps.rm.rx.service.rxjava3.database.resourceprovider.ResourceProviderService;
import at.uibk.dps.rm.rx.service.rxjava3.database.metric.MetricService;
import at.uibk.dps.rm.rx.service.rxjava3.database.metric.MetricValueService;
import at.uibk.dps.rm.rx.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.rx.service.rxjava3.database.resource.ResourceTypeService;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.VPCService;
import at.uibk.dps.rm.rx.service.rxjava3.database.service.ServiceService;
import at.uibk.dps.rm.rx.service.rxjava3.database.service.K8sServiceTypeService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentExecutionService;
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
    private final AccountNamespaceService accountNamespaceService;
    private final AccountService accountService;
    private final CredentialsService credentialsService;
    private final EnsembleService ensembleService;
    private final EnvironmentService environmentService;
    private final FunctionService functionService;
    private final FunctionTypeService functionTypeService;
    private final LogService logService;
    private final K8sServiceTypeService k8sServiceTypeService;
    private final MetricService metricService;
    private final MetricValueService metricValueService;
    private final NamespaceService namespaceService;
    private final PlatformService platformService;
    private final RegionService regionService;
    private final DeploymentService deploymentService;
    private final DeploymentLogService deploymentLogService;
    private final ResourceEnsembleService resourceEnsembleService;
    private final ResourceService resourceService;
    private final ResourceProviderService resourceProviderService;
    private final ResourceDeploymentService resourceDeploymentService;
    private final ResourceTypeService resourceTypeService;
    private final PlatformMetricService platformMetricService;
    private final RuntimeService runtimeService;
    private final ServiceDeploymentService serviceDeploymentService;
    private final ServiceService serviceService;
    private final ServiceTypeService serviceTypeService;
    private final VPCService vpcService;
    private final DeploymentExecutionService deploymentExecutionService;
    private final FilePathService filePathService;

    /**
     * Create an instance from vertx. All client proxies get setup in this method.
     *
     * @param vertx the current vertx instance
     */
    public ServiceProxyProvider(Vertx vertx) {
        accountNamespaceService = AccountNamespaceService.createProxy(vertx);
        accountService = AccountService.createProxy(vertx);
        credentialsService = CredentialsService.createProxy(vertx);
        ensembleService = EnsembleService.createProxy(vertx);
        environmentService = EnvironmentService.createProxy(vertx);
        functionService = FunctionService.createProxy(vertx);
        functionTypeService = FunctionTypeService.createProxy(vertx);
        k8sServiceTypeService = K8sServiceTypeService.createProxy(vertx);
        logService = LogService.createProxy(vertx);
        metricService = MetricService.createProxy(vertx);
        metricValueService = MetricValueService.createProxy(vertx);
        namespaceService = NamespaceService.createProxy(vertx);
        platformService = PlatformService.createProxy(vertx);
        platformMetricService = PlatformMetricService.createProxy(vertx);
        regionService = RegionService.createProxy(vertx);
        deploymentService = DeploymentService.createProxy(vertx);
        deploymentLogService = DeploymentLogService.createProxy(vertx);
        resourceEnsembleService = ResourceEnsembleService.createProxy(vertx);
        resourceService = ResourceService.createProxy(vertx);
        resourceProviderService = ResourceProviderService.createProxy(vertx);
        resourceDeploymentService = ResourceDeploymentService.createProxy(vertx);
        resourceTypeService = ResourceTypeService.createProxy(vertx);
        runtimeService = RuntimeService.createProxy(vertx);
        serviceDeploymentService = ServiceDeploymentService.createProxy(vertx);
        serviceService = ServiceService.createProxy(vertx);
        serviceTypeService = ServiceTypeService.createProxy(vertx);
        vpcService = VPCService.createProxy(vertx);
        deploymentExecutionService = DeploymentExecutionService.createProxy(vertx);
        filePathService = FilePathService.createProxy(vertx);
    }
}
