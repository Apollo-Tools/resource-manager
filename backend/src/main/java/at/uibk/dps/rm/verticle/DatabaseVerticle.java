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

            serviceProxyBinder.bind(AccountService.class,
                new AccountServiceImpl(new AccountRepository(), sessionFactory));
            serviceProxyBinder.bind(AccountCredentialsService.class,
                new AccountCredentialsServiceImpl(new AccountCredentialsRepository(), sessionFactory));
            serviceProxyBinder.bind(CredentialsService.class,
                new CredentialsServiceImpl(new CredentialsRepository(), sessionFactory));
            serviceProxyBinder.bind(EnsembleService.class,
                new EnsembleServiceImpl(new EnsembleRepository(), sessionFactory));
            serviceProxyBinder.bind(EnsembleSLOService.class,
                new EnsembleSLOServiceImpl(new EnsembleSLORepository(), sessionFactory));
            serviceProxyBinder.bind(EnvironmentService.class,
                new EnvironmentServiceImpl(new EnvironmentRepository(), sessionFactory));
            serviceProxyBinder.bind(FunctionService.class,
                new FunctionServiceImpl(new FunctionRepository(), sessionFactory));
            serviceProxyBinder.bind(FunctionDeploymentService.class,
                new FunctionDeploymentServiceImpl(new FunctionDeploymentRepository(), sessionFactory));
            serviceProxyBinder.bind(LogService.class, new LogServiceImpl(new LogRepository(), sessionFactory));
            serviceProxyBinder.bind(MetricService.class, new MetricServiceImpl(new MetricRepository(), sessionFactory));
            serviceProxyBinder.bind(MetricTypeService.class,
                new MetricTypeServiceImpl(new MetricTypeRepository(), sessionFactory));
            serviceProxyBinder.bind(MetricValueService.class,
                new MetricValueServiceImpl(new MetricValueRepository(), sessionFactory));
            serviceProxyBinder.bind(PlatformService.class,
                new PlatformServiceImpl(new PlatformRepository(), sessionFactory));
            serviceProxyBinder.bind(RegionService.class, new RegionServiceImpl(new RegionRepository(), sessionFactory));
            serviceProxyBinder.bind(DeploymentService.class,
                new DeploymentServiceImpl(new DeploymentRepository(), sessionFactory));
            serviceProxyBinder.bind(DeploymentLogService.class,
                new DeploymentLogServiceImpl(new DeploymentLogRepository(), sessionFactory));
            serviceProxyBinder.bind(ResourceEnsembleService.class,
                new ResourceEnsembleServiceImpl(new ResourceEnsembleRepository(), sessionFactory));
            serviceProxyBinder.bind(ResourceService.class,
                new ResourceServiceImpl(new ResourceRepository(), sessionFactory));
            serviceProxyBinder.bind(ResourceProviderService.class,
                new ResourceProviderServiceImpl(new ResourceProviderRepository(), sessionFactory));
            serviceProxyBinder.bind(ResourceDeploymentService.class,
                new ResourceDeploymentServiceImpl(new ResourceDeploymentRepository(), sessionFactory));
            serviceProxyBinder.bind(ResourceDeploymentStatusService.class,
                new ResourceDeploymentStatusServiceImpl(new ResourceDeploymentStatusRepository(), sessionFactory));
            serviceProxyBinder.bind(ResourceTypeService.class,
                new ResourceTypeServiceImpl(new ResourceTypeRepository(), sessionFactory));
            serviceProxyBinder.bind(PlatformMetricService.class,
                new PlatformMetricServiceImpl(new PlatformMetricRepository(), sessionFactory));
            serviceProxyBinder.bind(RuntimeService.class,
                new RuntimeServiceImpl(new RuntimeRepository(), sessionFactory));
            serviceProxyBinder.bind(ServiceService.class,
                new ServiceServiceImpl(new ServiceRepository(), sessionFactory));
            serviceProxyBinder.bind(ServiceDeploymentService.class,
                new ServiceDeploymentServiceImpl(new ServiceDeploymentRepository(), sessionFactory));
            serviceProxyBinder.bind(ServiceTypeService.class,
                new ServiceTypeServiceImpl(new ServiceTypeRepository(), sessionFactory));
            serviceProxyBinder.bind(VPCService.class, new VPCServiceImpl(new VPCRepository(), sessionFactory));
            serviceProxyBinder.bind(FilePathService.class, new FilePathServiceImpl(vertx.getDelegate()));
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupEventBus);
    }
}
