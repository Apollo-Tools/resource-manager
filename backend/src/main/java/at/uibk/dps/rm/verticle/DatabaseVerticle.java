package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.repository.DeploymentRepositoryProvider;
import at.uibk.dps.rm.rx.repository.EnsembleRepositoryProvider;
import at.uibk.dps.rm.rx.repository.account.*;
import at.uibk.dps.rm.rx.repository.artifact.FunctionTypeRepository;
import at.uibk.dps.rm.rx.repository.artifact.ServiceTypeRepository;
import at.uibk.dps.rm.rx.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.rx.repository.ensemble.EnsembleSLORepository;
import at.uibk.dps.rm.rx.repository.ensemble.ResourceEnsembleRepository;
import at.uibk.dps.rm.rx.repository.function.FunctionRepository;
import at.uibk.dps.rm.rx.repository.function.RuntimeRepository;
import at.uibk.dps.rm.rx.repository.log.LogRepository;
import at.uibk.dps.rm.rx.repository.log.DeploymentLogRepository;
import at.uibk.dps.rm.rx.repository.metric.MetricRepository;
import at.uibk.dps.rm.rx.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.rx.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.repository.deployment.*;
import at.uibk.dps.rm.rx.repository.resource.ResourceRepository;
import at.uibk.dps.rm.rx.repository.resource.PlatformRepository;
import at.uibk.dps.rm.rx.repository.resource.ResourceTypeRepository;
import at.uibk.dps.rm.rx.repository.resourceprovider.EnvironmentRepository;
import at.uibk.dps.rm.rx.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.rx.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.rx.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.rx.repository.service.ServiceRepository;
import at.uibk.dps.rm.rx.repository.service.K8sServiceTypeRepository;
import at.uibk.dps.rm.rx.service.database.account.*;
import at.uibk.dps.rm.rx.service.database.artifact.FunctionTypeService;
import at.uibk.dps.rm.rx.service.database.artifact.FunctionTypeServiceImpl;
import at.uibk.dps.rm.rx.service.database.artifact.ServiceTypeService;
import at.uibk.dps.rm.rx.service.database.artifact.ServiceTypeServiceImpl;
import at.uibk.dps.rm.rx.service.database.account.NamespaceService;
import at.uibk.dps.rm.rx.service.database.ensemble.*;
import at.uibk.dps.rm.rx.service.database.function.FunctionService;
import at.uibk.dps.rm.rx.service.database.function.FunctionServiceImpl;
import at.uibk.dps.rm.rx.service.database.function.RuntimeService;
import at.uibk.dps.rm.rx.service.database.function.RuntimeServiceImpl;
import at.uibk.dps.rm.rx.service.database.log.DeploymentLogService;
import at.uibk.dps.rm.rx.service.database.log.LogService;
import at.uibk.dps.rm.rx.service.database.log.LogServiceImpl;
import at.uibk.dps.rm.rx.service.database.log.DeploymentLogServiceImpl;
import at.uibk.dps.rm.service.database.deployment.*;
import at.uibk.dps.rm.rx.service.database.resource.ResourceService;
import at.uibk.dps.rm.rx.service.database.resource.ResourceServiceImpl;
import at.uibk.dps.rm.rx.service.database.resource.ResourceTypeService;
import at.uibk.dps.rm.rx.service.database.resource.ResourceTypeServiceImpl;
import at.uibk.dps.rm.rx.service.database.resource.PlatformService;
import at.uibk.dps.rm.rx.service.database.resource.PlatformServiceImpl;
import at.uibk.dps.rm.rx.service.database.resourceprovider.*;
import at.uibk.dps.rm.rx.service.database.metric.MetricService;
import at.uibk.dps.rm.rx.service.database.metric.MetricServiceImpl;
import at.uibk.dps.rm.rx.service.database.metric.MetricValueService;
import at.uibk.dps.rm.rx.service.database.metric.MetricValueServiceImpl;
import at.uibk.dps.rm.rx.service.database.metric.PlatformMetricService;
import at.uibk.dps.rm.rx.service.database.metric.PlatformMetricServiceImpl;
import at.uibk.dps.rm.rx.service.database.service.ServiceService;
import at.uibk.dps.rm.rx.service.database.service.ServiceServiceImpl;
import at.uibk.dps.rm.rx.service.database.service.K8sServiceTypeService;
import at.uibk.dps.rm.rx.service.database.service.K8sServiceTypeServiceImpl;
import at.uibk.dps.rm.service.util.FilePathService;
import at.uibk.dps.rm.service.util.FilePathServiceImpl;
import at.uibk.dps.rm.service.ServiceProxyBinder;
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
        return setupDatabase()
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMapCompletable(res -> setupEventBus());
    }

    /**
     * Set up the connection to the database
     *
     * @return a Completable
     */
    private Completable setupDatabase() {
        Maybe<Void> startDB = vertx.rxExecuteBlocking(emitter -> {
            int dbPort = config().getInteger("db_port");
            String dbHost = config().getString("db_host");
            String dbUser = config().getString("db_user");
            String dbPassword = config().getString("db_password");
            Map<String, String> props = Map.of(
                "javax.persistence.jdbc.url", "jdbc:postgresql://" + dbHost + ":" + dbPort + "/resource-manager",
                "javax.persistence.jdbc.user", dbUser,
                "javax.persistence.jdbc.password", dbPassword);
            sessionFactory = Persistence
                    .createEntityManagerFactory("postgres-unit", props)
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
            at.uibk.dps.rm.rx.service.ServiceProxyBinder rxServiceProxyBinder = new at.uibk.dps.rm.rx.service.ServiceProxyBinder(serviceBinder);

            rxServiceProxyBinder.bind(AccountNamespaceService.class,
                new AccountNamespaceServiceImpl(new AccountNamespaceRepository(), sessionFactory));
            rxServiceProxyBinder.bind(AccountService.class,
                new AccountServiceImpl(new AccountRepository(), new RoleRepository(), sessionFactory));
            rxServiceProxyBinder.bind(CredentialsService.class, new CredentialsServiceImpl(new CredentialsRepository(),
                new AccountCredentialsRepository(), sessionFactory));
            rxServiceProxyBinder.bind(EnsembleService.class, new EnsembleServiceImpl(new EnsembleRepositoryProvider(),
                sessionFactory));
            rxServiceProxyBinder.bind(EnvironmentService.class,
                new EnvironmentServiceImpl(new EnvironmentRepository(), sessionFactory));
            rxServiceProxyBinder.bind(FunctionService.class,
                new FunctionServiceImpl(new FunctionRepository(), sessionFactory));
            rxServiceProxyBinder.bind(FunctionTypeService.class,
                new FunctionTypeServiceImpl(new FunctionTypeRepository(), sessionFactory));
            rxServiceProxyBinder.bind(LogService.class, new LogServiceImpl(new LogRepository(), sessionFactory));
            rxServiceProxyBinder.bind(MetricService.class, new MetricServiceImpl(new MetricRepository(),
                sessionFactory));
            rxServiceProxyBinder.bind(MetricValueService.class,
                new MetricValueServiceImpl(new MetricValueRepository(), new PlatformMetricRepository(), sessionFactory)
            );
            rxServiceProxyBinder.bind(NamespaceService.class,
                new NamespaceServiceImpl(new NamespaceRepository(), new ResourceRepository(), sessionFactory));
            rxServiceProxyBinder.bind(PlatformService.class,
                new PlatformServiceImpl(new PlatformRepository(), sessionFactory));
            rxServiceProxyBinder.bind(RegionService.class, new RegionServiceImpl(new RegionRepository(),
                new ResourceProviderRepository(), sessionFactory));
            serviceProxyBinder.bind(DeploymentService.class,
                new DeploymentServiceImpl(new DeploymentRepositoryProvider(), sessionFactory));
            rxServiceProxyBinder.bind(DeploymentLogService.class,
                new DeploymentLogServiceImpl(new DeploymentLogRepository(), sessionFactory));
            rxServiceProxyBinder.bind(K8sServiceTypeService.class,
                    new K8sServiceTypeServiceImpl(new K8sServiceTypeRepository(), sessionFactory));
            rxServiceProxyBinder.bind(ResourceEnsembleService.class,
                new ResourceEnsembleServiceImpl(new ResourceEnsembleRepository(), new EnsembleSLORepository(),
                    new EnsembleRepository(), new ResourceRepository(), sessionFactory));
            rxServiceProxyBinder.bind(ResourceService.class,
                new ResourceServiceImpl(new ResourceRepository(),
                    new at.uibk.dps.rm.rx.repository.resourceprovider.RegionRepository(), new MetricRepository(),
                    sessionFactory));
            rxServiceProxyBinder.bind(ResourceProviderService.class,
                new ResourceProviderServiceImpl(new ResourceProviderRepository(), sessionFactory));
            serviceProxyBinder.bind(ResourceDeploymentService.class,
                new ResourceDeploymentServiceImpl(new ResourceDeploymentRepository(), sessionFactory));
            rxServiceProxyBinder.bind(ResourceTypeService.class,
                new ResourceTypeServiceImpl(new ResourceTypeRepository(), sessionFactory));
            rxServiceProxyBinder.bind(PlatformMetricService.class,
                new PlatformMetricServiceImpl(new PlatformMetricRepository(), sessionFactory));
            rxServiceProxyBinder.bind(RuntimeService.class,
                new RuntimeServiceImpl(new RuntimeRepository(), sessionFactory));
            rxServiceProxyBinder.bind(ServiceService.class,
                new ServiceServiceImpl(new ServiceRepository(), sessionFactory));
            serviceProxyBinder.bind(ServiceDeploymentService.class,
                new ServiceDeploymentServiceImpl(new ServiceDeploymentRepository(), sessionFactory));
            rxServiceProxyBinder.bind(ServiceTypeService.class,
                new ServiceTypeServiceImpl(new ServiceTypeRepository(), sessionFactory));
            rxServiceProxyBinder.bind(VPCService.class, new VPCServiceImpl(new VPCRepository(), new RegionRepository(),
                sessionFactory));
            serviceProxyBinder.bind(FilePathService.class, new FilePathServiceImpl(vertx.getDelegate()));
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupEventBus);
    }
}
