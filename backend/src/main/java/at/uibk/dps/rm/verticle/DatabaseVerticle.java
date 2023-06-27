package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.repository.account.AccountCredentialsRepository;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
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
import at.uibk.dps.rm.repository.service.ServiceTypeRepository;
import at.uibk.dps.rm.service.database.account.*;
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
import at.uibk.dps.rm.service.database.service.ServiceTypeService;
import at.uibk.dps.rm.service.database.service.ServiceTypeServiceImpl;
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

    private AccountRepository accountRepository;
    private AccountCredentialsRepository accountCredentialsRepository;
    private CredentialsRepository credentialsRepository;
    private EnsembleRepository ensembleRepository;
    private EnsembleSLORepository ensembleSLORepository;
    private EnvironmentRepository environmentRepository;
    private FunctionRepository functionRepository;
    private FunctionDeploymentRepository functionDeploymentRepository;
    private LogRepository logRepository;
    private MetricRepository metricRepository;
    private MetricTypeRepository metricTypeRepository;
    private MetricValueRepository metricValueRepository;
    private PlatformRepository platformRepository;
    private RegionRepository regionRepository;
    private DeploymentRepository deploymentRepository;
    private DeploymentLogRepository deploymentLogRepository;
    private ResourceEnsembleRepository resourceEnsembleRepository;
    private ResourceRepository resourceRepository;
    private ResourceProviderRepository resourceProviderRepository;
    private ResourceDeploymentRepository resourceDeploymentRepository;
    private ResourceDeploymentStatusRepository statusRepository;
    private ResourceTypeRepository resourceTypeRepository;
    private PlatformMetricRepository platformMetricRepository;
    private RuntimeRepository runtimeRepository;
    private ServiceRepository serviceRepository;
    private ServiceDeploymentRepository serviceDeploymentRepository;
    private ServiceTypeRepository serviceTypeRepository;
    private VPCRepository vpcRepository;

    @Override
    public Completable rxStart() {
        return setupDatabase()
                .andThen(initializeRepositories())
                .andThen(setupEventBus());
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
     * Initialize all repositories.
     *
     * @return a Completable
     */
    private Completable initializeRepositories() {
        Maybe<Void> setupServices = Maybe.create(emitter -> {
            accountRepository = new AccountRepository(sessionFactory);
            accountCredentialsRepository = new AccountCredentialsRepository(sessionFactory);
            credentialsRepository = new CredentialsRepository(sessionFactory);
            ensembleRepository = new EnsembleRepository(sessionFactory);
            ensembleSLORepository = new EnsembleSLORepository(sessionFactory);
            environmentRepository = new EnvironmentRepository(sessionFactory);
            functionRepository = new FunctionRepository(sessionFactory);
            functionDeploymentRepository = new FunctionDeploymentRepository(sessionFactory);
            logRepository = new LogRepository(sessionFactory);
            metricRepository = new MetricRepository(sessionFactory);
            metricTypeRepository = new MetricTypeRepository(sessionFactory);
            metricValueRepository = new MetricValueRepository(sessionFactory);
            platformRepository = new PlatformRepository(sessionFactory);
            regionRepository = new RegionRepository(sessionFactory);
            deploymentRepository = new DeploymentRepository(sessionFactory);
            deploymentLogRepository = new DeploymentLogRepository(sessionFactory);
            resourceEnsembleRepository = new ResourceEnsembleRepository(sessionFactory);
            resourceRepository = new ResourceRepository(sessionFactory);
            resourceProviderRepository = new ResourceProviderRepository(sessionFactory);
            resourceDeploymentRepository = new ResourceDeploymentRepository(sessionFactory);
            statusRepository = new ResourceDeploymentStatusRepository(sessionFactory);
            resourceTypeRepository = new ResourceTypeRepository(sessionFactory);
            platformMetricRepository = new PlatformMetricRepository(sessionFactory);
            runtimeRepository = new RuntimeRepository(sessionFactory);
            serviceRepository = new ServiceRepository(sessionFactory);
            serviceDeploymentRepository = new ServiceDeploymentRepository(sessionFactory);
            serviceTypeRepository = new ServiceTypeRepository(sessionFactory);
            vpcRepository = new VPCRepository(sessionFactory);
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupServices);
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

            serviceProxyBinder.bind(AccountService.class, new AccountServiceImpl(accountRepository, sessionFactory));
            serviceProxyBinder.bind(AccountCredentialsService.class,
                new AccountCredentialsServiceImpl(accountCredentialsRepository, sessionFactory));
            serviceProxyBinder.bind(CredentialsService.class,
                new CredentialsServiceImpl(credentialsRepository, sessionFactory));
            serviceProxyBinder.bind(EnsembleService.class, new EnsembleServiceImpl(ensembleRepository, sessionFactory));
            serviceProxyBinder.bind(EnsembleSLOService.class,
                new EnsembleSLOServiceImpl(ensembleSLORepository, sessionFactory));
            serviceProxyBinder.bind(EnvironmentService.class,
                new EnvironmentServiceImpl(environmentRepository, sessionFactory));
            serviceProxyBinder.bind(FunctionService.class, new FunctionServiceImpl(functionRepository, sessionFactory));
            serviceProxyBinder.bind(FunctionDeploymentService.class,
                new FunctionDeploymentServiceImpl(functionDeploymentRepository, sessionFactory));
            serviceProxyBinder.bind(LogService.class, new LogServiceImpl(logRepository, sessionFactory));
            serviceProxyBinder.bind(MetricService.class, new MetricServiceImpl(metricRepository, sessionFactory));
            serviceProxyBinder.bind(MetricTypeService.class,
                new MetricTypeServiceImpl(metricTypeRepository, sessionFactory));
            serviceProxyBinder.bind(MetricValueService.class,
                new MetricValueServiceImpl(metricValueRepository, sessionFactory));
            serviceProxyBinder.bind(PlatformService.class, new PlatformServiceImpl(platformRepository, sessionFactory));
            serviceProxyBinder.bind(RegionService.class, new RegionServiceImpl(regionRepository, sessionFactory));
            serviceProxyBinder.bind(DeploymentService.class,
                new DeploymentServiceImpl(deploymentRepository, sessionFactory));
            serviceProxyBinder.bind(DeploymentLogService.class,
                new DeploymentLogServiceImpl(deploymentLogRepository, sessionFactory));
            serviceProxyBinder.bind(ResourceEnsembleService.class,
                new ResourceEnsembleServiceImpl(resourceEnsembleRepository, sessionFactory));
            serviceProxyBinder.bind(ResourceService.class, new ResourceServiceImpl(resourceRepository, sessionFactory));
            serviceProxyBinder.bind(ResourceProviderService.class,
                new ResourceProviderServiceImpl(resourceProviderRepository, sessionFactory));
            serviceProxyBinder.bind(ResourceDeploymentService.class,
                new ResourceDeploymentServiceImpl(resourceDeploymentRepository, sessionFactory));
            serviceProxyBinder.bind(ResourceDeploymentStatusService.class,
                new ResourceDeploymentStatusServiceImpl(statusRepository, sessionFactory));
            serviceProxyBinder.bind(ResourceTypeService.class,
                new ResourceTypeServiceImpl(resourceTypeRepository, sessionFactory));
            serviceProxyBinder.bind(PlatformMetricService.class,
                new PlatformMetricServiceImpl(platformMetricRepository, sessionFactory));
            serviceProxyBinder.bind(RuntimeService.class, new RuntimeServiceImpl(runtimeRepository, sessionFactory));
            serviceProxyBinder.bind(ServiceService.class, new ServiceServiceImpl(serviceRepository, sessionFactory));
            serviceProxyBinder.bind(ServiceDeploymentService.class,
                new ServiceDeploymentServiceImpl(serviceDeploymentRepository, sessionFactory));
            serviceProxyBinder.bind(ServiceTypeService.class,
                new ServiceTypeServiceImpl(serviceTypeRepository, sessionFactory));
            serviceProxyBinder.bind(VPCService.class, new VPCServiceImpl(vpcRepository, sessionFactory));
            serviceProxyBinder.bind(FilePathService.class, new FilePathServiceImpl(vertx.getDelegate()));
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupEventBus);
    }
}
