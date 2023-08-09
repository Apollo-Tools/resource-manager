package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.repository.DeploymentRepositoryProvider;
import at.uibk.dps.rm.repository.EnsembleRepositoryProvider;
import at.uibk.dps.rm.repository.account.*;
import at.uibk.dps.rm.repository.artifact.FunctionTypeRepository;
import at.uibk.dps.rm.repository.artifact.ServiceTypeRepository;
import at.uibk.dps.rm.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.repository.ensemble.EnsembleSLORepository;
import at.uibk.dps.rm.repository.ensemble.ResourceEnsembleRepository;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.repository.function.RuntimeRepository;
import at.uibk.dps.rm.repository.log.LogRepository;
import at.uibk.dps.rm.repository.log.DeploymentLogRepository;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.metric.MetricTypeRepository;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.repository.deployment.*;
import at.uibk.dps.rm.repository.resource.PlatformRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
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
import at.uibk.dps.rm.service.database.ensemble.*;
import at.uibk.dps.rm.service.database.function.*;
import at.uibk.dps.rm.service.database.log.DeploymentLogService;
import at.uibk.dps.rm.service.database.log.LogService;
import at.uibk.dps.rm.service.database.log.LogServiceImpl;
import at.uibk.dps.rm.service.database.log.DeploymentLogServiceImpl;
import at.uibk.dps.rm.service.database.deployment.*;
import at.uibk.dps.rm.service.database.resource.*;
import at.uibk.dps.rm.service.database.resourceprovider.*;
import at.uibk.dps.rm.service.database.metric.*;
import at.uibk.dps.rm.service.database.service.ServiceService;
import at.uibk.dps.rm.service.database.service.ServiceServiceImpl;
import at.uibk.dps.rm.service.database.service.K8sServiceTypeService;
import at.uibk.dps.rm.service.database.service.K8sServiceTypeServiceImpl;
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

            serviceProxyBinder.bind(AccountNamespaceService.class,
                new AccountNamespaceServiceImpl(new AccountNamespaceRepository(), sessionFactory));
            serviceProxyBinder.bind(AccountService.class,
                new AccountServiceImpl(new AccountRepository(), sessionFactory));
            serviceProxyBinder.bind(CredentialsService.class, new CredentialsServiceImpl(new CredentialsRepository(),
                new AccountRepository(), new AccountCredentialsRepository(), new ResourceProviderRepository(),
                sessionFactory));
            serviceProxyBinder.bind(EnsembleService.class, new EnsembleServiceImpl(new EnsembleRepositoryProvider(),
                sessionFactory));
            serviceProxyBinder.bind(EnvironmentService.class,
                new EnvironmentServiceImpl(new EnvironmentRepository(), sessionFactory));
            serviceProxyBinder.bind(FunctionService.class,
                new FunctionServiceImpl(new FunctionRepository(), new RuntimeRepository(), sessionFactory));
            serviceProxyBinder.bind(FunctionTypeService.class,
                new FunctionTypeServiceImpl(new FunctionTypeRepository(), sessionFactory));
            serviceProxyBinder.bind(LogService.class, new LogServiceImpl(new LogRepository(), sessionFactory));
            serviceProxyBinder.bind(MetricService.class, new MetricServiceImpl(new MetricRepository(), sessionFactory));
            serviceProxyBinder.bind(MetricTypeService.class,
                new MetricTypeServiceImpl(new MetricTypeRepository(), sessionFactory));
            serviceProxyBinder.bind(MetricValueService.class,
                new MetricValueServiceImpl(new MetricValueRepository(), sessionFactory));
            serviceProxyBinder.bind(NamespaceService.class,
                new NamespaceServiceImpl(new NamespaceRepository(), new ResourceRepository(), sessionFactory));
            serviceProxyBinder.bind(PlatformService.class,
                new PlatformServiceImpl(new PlatformRepository(), sessionFactory));
            serviceProxyBinder.bind(RegionService.class, new RegionServiceImpl(new RegionRepository(),
                new ResourceProviderRepository(), sessionFactory));
            serviceProxyBinder.bind(DeploymentService.class,
                new DeploymentServiceImpl(new DeploymentRepositoryProvider(), sessionFactory));
            serviceProxyBinder.bind(DeploymentLogService.class,
                new DeploymentLogServiceImpl(new DeploymentLogRepository(), sessionFactory));
            serviceProxyBinder.bind(K8sServiceTypeService.class,
                    new K8sServiceTypeServiceImpl(new K8sServiceTypeRepository(), sessionFactory));
            serviceProxyBinder.bind(ResourceEnsembleService.class,
                new ResourceEnsembleServiceImpl(new ResourceEnsembleRepository(), new EnsembleSLORepository(),
                    new EnsembleRepository(), new ResourceRepository(), sessionFactory));
            serviceProxyBinder.bind(ResourceService.class,
                new ResourceServiceImpl(new ResourceRepository(), new RegionRepository(), new MetricRepository(),
                    sessionFactory));
            serviceProxyBinder.bind(ResourceProviderService.class,
                new ResourceProviderServiceImpl(new ResourceProviderRepository(), sessionFactory));
            serviceProxyBinder.bind(ResourceDeploymentService.class,
                new ResourceDeploymentServiceImpl(new ResourceDeploymentRepository(), sessionFactory));
            serviceProxyBinder.bind(ResourceTypeService.class,
                new ResourceTypeServiceImpl(new ResourceTypeRepository(), sessionFactory));
            serviceProxyBinder.bind(PlatformMetricService.class,
                new PlatformMetricServiceImpl(new PlatformMetricRepository(), sessionFactory));
            serviceProxyBinder.bind(RuntimeService.class,
                new RuntimeServiceImpl(new RuntimeRepository(), sessionFactory));
            serviceProxyBinder.bind(ServiceService.class,
                new ServiceServiceImpl(new ServiceRepository(), new K8sServiceTypeRepository(), sessionFactory));
            serviceProxyBinder.bind(ServiceDeploymentService.class,
                new ServiceDeploymentServiceImpl(new ServiceDeploymentRepository(), sessionFactory));
            serviceProxyBinder.bind(ServiceTypeService.class,
                new ServiceTypeServiceImpl(new ServiceTypeRepository(), sessionFactory));
            serviceProxyBinder.bind(VPCService.class, new VPCServiceImpl(new VPCRepository(), new RegionRepository(),
                sessionFactory));
            serviceProxyBinder.bind(FilePathService.class, new FilePathServiceImpl(vertx.getDelegate()));
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupEventBus);
    }
}
