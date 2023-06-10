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
import at.uibk.dps.rm.service.database.log.LogService;
import at.uibk.dps.rm.service.database.log.LogServiceImpl;
import at.uibk.dps.rm.service.database.log.ReservationLogService;
import at.uibk.dps.rm.service.database.log.ReservationLogServiceImpl;
import at.uibk.dps.rm.service.database.reservation.*;
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

            serviceProxyBinder.bind(AccountService.class, new AccountServiceImpl(accountRepository));
            serviceProxyBinder.bind(AccountCredentialsService.class,
                new AccountCredentialsServiceImpl(accountCredentialsRepository));
            serviceProxyBinder.bind(CredentialsService.class, new CredentialsServiceImpl(credentialsRepository));
            serviceProxyBinder.bind(EnsembleService.class, new EnsembleServiceImpl(ensembleRepository));
            serviceProxyBinder.bind(EnsembleSLOService.class, new EnsembleSLOServiceImpl(ensembleSLORepository));
            serviceProxyBinder.bind(EnvironmentService.class, new EnvironmentServiceImpl(environmentRepository));
            serviceProxyBinder.bind(FunctionService.class, new FunctionServiceImpl(functionRepository));
            serviceProxyBinder.bind(FunctionReservationService.class,
                new FunctionReservationServiceImpl(functionDeploymentRepository));
            serviceProxyBinder.bind(LogService.class, new LogServiceImpl(logRepository));
            serviceProxyBinder.bind(MetricService.class, new MetricServiceImpl(metricRepository));
            serviceProxyBinder.bind(MetricTypeService.class, new MetricTypeServiceImpl(metricTypeRepository));
            serviceProxyBinder.bind(MetricValueService.class, new MetricValueServiceImpl(metricValueRepository));
            serviceProxyBinder.bind(PlatformService.class, new PlatformServiceImpl(platformRepository));
            serviceProxyBinder.bind(RegionService.class, new RegionServiceImpl(regionRepository));
            serviceProxyBinder.bind(ReservationService.class, new ReservationServiceImpl(deploymentRepository));
            serviceProxyBinder.bind(ReservationLogService.class,
                new ReservationLogServiceImpl(deploymentLogRepository));
            serviceProxyBinder.bind(ResourceEnsembleService.class,
                new ResourceEnsembleServiceImpl(resourceEnsembleRepository));
            serviceProxyBinder.bind(ResourceService.class, new ResourceServiceImpl(resourceRepository));
            serviceProxyBinder.bind(ResourceProviderService.class,
                new ResourceProviderServiceImpl(resourceProviderRepository));
            serviceProxyBinder.bind(ResourceReservationService.class,
                new ResourceReservationServiceImpl(resourceDeploymentRepository));
            serviceProxyBinder.bind(ResourceReservationStatusService.class,
                new ResourceReservationStatusServiceImpl(statusRepository));
            serviceProxyBinder.bind(ResourceTypeService.class, new ResourceTypeServiceImpl(resourceTypeRepository));
            serviceProxyBinder.bind(PlatformMetricService.class,
                new PlatformMetricServiceImpl(platformMetricRepository));
            serviceProxyBinder.bind(RuntimeService.class, new RuntimeServiceImpl(runtimeRepository));
            serviceProxyBinder.bind(ServiceService.class, new ServiceServiceImpl(serviceRepository));
            serviceProxyBinder.bind(ServiceReservationService.class,
                new ServiceReservationServiceImpl(serviceDeploymentRepository));
            serviceProxyBinder.bind(ServiceTypeService.class, new ServiceTypeServiceImpl(serviceTypeRepository));
            serviceProxyBinder.bind(VPCService.class, new VPCServiceImpl(vpcRepository));
            serviceProxyBinder.bind(FilePathService.class, new FilePathServiceImpl(vertx.getDelegate()));
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupEventBus);
    }
}
