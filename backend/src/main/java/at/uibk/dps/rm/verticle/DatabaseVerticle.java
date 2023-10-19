package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.repository.DeploymentRepositoryProvider;
import at.uibk.dps.rm.repository.EnsembleRepositoryProvider;
import at.uibk.dps.rm.repository.account.*;
import at.uibk.dps.rm.repository.artifact.FunctionTypeRepository;
import at.uibk.dps.rm.repository.artifact.ServiceTypeRepository;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.repository.function.RuntimeRepository;
import at.uibk.dps.rm.repository.log.LogRepository;
import at.uibk.dps.rm.repository.log.DeploymentLogRepository;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.repository.deployment.*;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resource.PlatformRepository;
import at.uibk.dps.rm.repository.resource.ResourceTypeRepository;
import at.uibk.dps.rm.repository.resourceprovider.EnvironmentRepository;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import at.uibk.dps.rm.repository.service.K8sServiceTypeRepository;
import at.uibk.dps.rm.service.database.account.*;
import at.uibk.dps.rm.service.database.artifact.FunctionTypeService;
import at.uibk.dps.rm.service.database.artifact.FunctionTypeServiceImpl;
import at.uibk.dps.rm.service.database.artifact.ServiceTypeService;
import at.uibk.dps.rm.service.database.artifact.ServiceTypeServiceImpl;
import at.uibk.dps.rm.service.database.account.NamespaceService;
import at.uibk.dps.rm.service.database.ensemble.*;
import at.uibk.dps.rm.service.database.function.FunctionService;
import at.uibk.dps.rm.service.database.function.FunctionServiceImpl;
import at.uibk.dps.rm.service.database.function.RuntimeService;
import at.uibk.dps.rm.service.database.function.RuntimeServiceImpl;
import at.uibk.dps.rm.service.database.log.DeploymentLogService;
import at.uibk.dps.rm.service.database.log.LogService;
import at.uibk.dps.rm.service.database.log.LogServiceImpl;
import at.uibk.dps.rm.service.database.log.DeploymentLogServiceImpl;
import at.uibk.dps.rm.service.database.deployment.*;
import at.uibk.dps.rm.service.database.deployment.DeploymentService;
import at.uibk.dps.rm.service.database.deployment.DeploymentServiceImpl;
import at.uibk.dps.rm.service.database.resource.ResourceService;
import at.uibk.dps.rm.service.database.resource.ResourceServiceImpl;
import at.uibk.dps.rm.service.database.resource.ResourceTypeService;
import at.uibk.dps.rm.service.database.resource.ResourceTypeServiceImpl;
import at.uibk.dps.rm.service.database.resource.PlatformService;
import at.uibk.dps.rm.service.database.resource.PlatformServiceImpl;
import at.uibk.dps.rm.service.database.resourceprovider.*;
import at.uibk.dps.rm.service.database.metric.MetricService;
import at.uibk.dps.rm.service.database.metric.MetricServiceImpl;
import at.uibk.dps.rm.service.database.metric.MetricValueService;
import at.uibk.dps.rm.service.database.metric.MetricValueServiceImpl;
import at.uibk.dps.rm.service.database.metric.PlatformMetricService;
import at.uibk.dps.rm.service.database.metric.PlatformMetricServiceImpl;
import at.uibk.dps.rm.service.database.service.ServiceService;
import at.uibk.dps.rm.service.database.service.ServiceServiceImpl;
import at.uibk.dps.rm.service.database.service.K8sServiceTypeService;
import at.uibk.dps.rm.service.database.service.K8sServiceTypeServiceImpl;
import at.uibk.dps.rm.service.ServiceProxyBinder;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import io.reactivex.rxjava3.core.*;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import javax.persistence.Persistence;
import java.util.Map;

/**
 * Everything that relates to database operations is executed on the DatabaseVerticle.
 *
 * @author matthi-g
 */
public class DatabaseVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseVerticle.class);

    private SessionFactory sessionFactory;

    @Override
    public Completable rxStart() {
        ConfigDTO config = config().mapTo(ConfigDTO.class);
        return setupDatabase(config)
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMapCompletable(res -> setupEventBus());
    }

    /**
     * Set up the connection to the database
     *
     * @return a Completable
     */
    private Completable setupDatabase(ConfigDTO config) {
        Maybe<Void> startDB = vertx.rxExecuteBlocking(emitter -> {
            Map<String, String> props = Map.of(
                "javax.persistence.jdbc.url", "jdbc:postgresql://" + config.getDbHost() + ":" +
                    config.getDbPort() + "/resource-manager",
                "javax.persistence.jdbc.user", config.getDbUser(),
                "javax.persistence.jdbc.password", config.getDbPassword());
            sessionFactory = Persistence.createEntityManagerFactory("postgres-unit", props)
                    .unwrap(SessionFactory.class);
            logger.info("Connection to database established");
            emitter.complete();
        });
        return Completable.fromMaybe(startDB);
    }

    /**
     * Register all database service proxies on the event bus.
     *
     * @return a Completable
     */
    private Completable setupEventBus() {
        Maybe<Void> setupEventBus = Maybe.create(emitter -> {
            ServiceBinder serviceBinder = new ServiceBinder(vertx.getDelegate());
            ServiceProxyBinder serviceProxyBinder = new ServiceProxyBinder(serviceBinder);
            SessionManagerProvider smProvider = new SessionManagerProvider(sessionFactory);

            serviceProxyBinder.bind(AccountNamespaceService.class,
                new AccountNamespaceServiceImpl(new AccountNamespaceRepository(), smProvider));
            serviceProxyBinder.bind(AccountService.class,
                new AccountServiceImpl(new AccountRepository(), new RoleRepository(), smProvider));
            serviceProxyBinder.bind(CredentialsService.class, new CredentialsServiceImpl(new CredentialsRepository(),
                new AccountCredentialsRepository(), smProvider));
            serviceProxyBinder.bind(EnsembleService.class, new EnsembleServiceImpl(new EnsembleRepositoryProvider(),
                smProvider));
            serviceProxyBinder.bind(EnvironmentService.class,
                new EnvironmentServiceImpl(new EnvironmentRepository(), smProvider));
            serviceProxyBinder.bind(FunctionService.class,
                new FunctionServiceImpl(new FunctionRepository(), smProvider));
            serviceProxyBinder.bind(FunctionTypeService.class,
                new FunctionTypeServiceImpl(new FunctionTypeRepository(), smProvider));
            serviceProxyBinder.bind(LogService.class, new LogServiceImpl(new LogRepository(), smProvider));
            serviceProxyBinder.bind(MetricService.class, new MetricServiceImpl(new MetricRepository(),
                smProvider));
            serviceProxyBinder.bind(MetricValueService.class,
                new MetricValueServiceImpl(new MetricValueRepository(), new PlatformMetricRepository(), smProvider)
            );
            serviceProxyBinder.bind(NamespaceService.class,
                new NamespaceServiceImpl(new NamespaceRepository(), new ResourceRepository(), smProvider));
            serviceProxyBinder.bind(PlatformService.class,
                new PlatformServiceImpl(new PlatformRepository(), smProvider));
            serviceProxyBinder.bind(RegionService.class, new RegionServiceImpl(new RegionRepository(), smProvider));
            serviceProxyBinder.bind(DeploymentService.class,
                new DeploymentServiceImpl(new DeploymentRepositoryProvider(), smProvider));
            serviceProxyBinder.bind(DeploymentLogService.class,
                new DeploymentLogServiceImpl(new DeploymentLogRepository(), smProvider));
            serviceProxyBinder.bind(K8sServiceTypeService.class,
                    new K8sServiceTypeServiceImpl(new K8sServiceTypeRepository(), smProvider));
            serviceProxyBinder.bind(ResourceEnsembleService.class,
                new ResourceEnsembleServiceImpl(new EnsembleRepositoryProvider(), smProvider));
            serviceProxyBinder.bind(ResourceService.class,
                new ResourceServiceImpl(new ResourceRepository(),
                    new RegionRepository(), new MetricRepository(),
                    smProvider));
            serviceProxyBinder.bind(ResourceProviderService.class,
                new ResourceProviderServiceImpl(new ResourceProviderRepository(), smProvider));
            serviceProxyBinder.bind(ResourceDeploymentService.class,
                new ResourceDeploymentServiceImpl(new ResourceDeploymentRepository(), smProvider));
            serviceProxyBinder.bind(ResourceTypeService.class,
                new ResourceTypeServiceImpl(new ResourceTypeRepository(), smProvider));
            serviceProxyBinder.bind(PlatformMetricService.class,
                new PlatformMetricServiceImpl(new PlatformMetricRepository(), smProvider));
            serviceProxyBinder.bind(RuntimeService.class,
                new RuntimeServiceImpl(new RuntimeRepository(), smProvider));
            serviceProxyBinder.bind(ServiceService.class,
                new ServiceServiceImpl(new ServiceRepository(), smProvider));
            serviceProxyBinder.bind(ServiceDeploymentService.class,
                new ServiceDeploymentServiceImpl(new ServiceDeploymentRepository(), smProvider));
            serviceProxyBinder.bind(ServiceTypeService.class,
                new ServiceTypeServiceImpl(new ServiceTypeRepository(), smProvider));
            serviceProxyBinder.bind(VPCService.class, new VPCServiceImpl(new VPCRepository(), smProvider));
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupEventBus);
    }
}
