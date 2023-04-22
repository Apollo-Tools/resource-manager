package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.repository.account.AccountCredentialsRepository;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.repository.function.FunctionResourceRepository;
import at.uibk.dps.rm.repository.function.RuntimeRepository;
import at.uibk.dps.rm.repository.log.LogRepository;
import at.uibk.dps.rm.repository.log.ReservationLogRepository;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.metric.MetricTypeRepository;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.repository.metric.ResourceTypeMetricRepository;
import at.uibk.dps.rm.repository.reservation.ReservationRepository;
import at.uibk.dps.rm.repository.reservation.ResourceReservationRepository;
import at.uibk.dps.rm.repository.reservation.ResourceReservationStatusRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resource.ResourceTypeRepository;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.service.database.account.*;
import at.uibk.dps.rm.service.database.function.*;
import at.uibk.dps.rm.service.database.log.LogService;
import at.uibk.dps.rm.service.database.log.LogServiceImpl;
import at.uibk.dps.rm.service.database.log.ReservationLogService;
import at.uibk.dps.rm.service.database.log.ReservationLogServiceImpl;
import at.uibk.dps.rm.service.database.reservation.*;
import at.uibk.dps.rm.service.database.resourceprovider.*;
import at.uibk.dps.rm.service.database.metric.*;
import at.uibk.dps.rm.service.database.resource.ResourceService;
import at.uibk.dps.rm.service.database.resource.ResourceServiceImpl;
import at.uibk.dps.rm.service.database.resource.ResourceTypeService;
import at.uibk.dps.rm.service.database.resource.ResourceTypeServiceImpl;
import at.uibk.dps.rm.service.util.FilePathService;
import at.uibk.dps.rm.service.util.FilePathServiceImpl;
import at.uibk.dps.rm.util.ServiceProxyBinder;
import io.reactivex.rxjava3.core.*;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import javax.persistence.Persistence;
import java.util.Map;

public class DatabaseVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseVerticle.class);

    private SessionFactory sessionFactory;

    private AccountRepository accountRepository;
    private AccountCredentialsRepository accountCredentialsRepository;
    private CredentialsRepository credentialsRepository;
    private FunctionRepository functionRepository;
    private FunctionResourceRepository functionResourceRepository;
    private LogRepository logRepository;
    private MetricRepository metricRepository;
    private MetricTypeRepository metricTypeRepository;
    private MetricValueRepository metricValueRepository;
    private RegionRepository regionRepository;
    private ReservationRepository reservationRepository;
    private ReservationLogRepository reservationLogRepository;
    private ResourceRepository resourceRepository;
    private ResourceProviderRepository resourceProviderRepository;
    private ResourceReservationRepository resourceReservationRepository;
    private ResourceReservationStatusRepository statusRepository;
    private ResourceTypeRepository resourceTypeRepository;
    private ResourceTypeMetricRepository resourceTypeMetricRepository;
    private RuntimeRepository runtimeRepository;
    private VPCRepository vpcRepository;

    @Override
    public Completable rxStart() {
        return setupDatabase()
                .andThen(initializeRepositories())
                .andThen(setupEventBus());
    }

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

    private Completable initializeRepositories() {
        Maybe<Void> setupServices = Maybe.create(emitter -> {
            accountRepository = new AccountRepository(sessionFactory);
            accountCredentialsRepository = new AccountCredentialsRepository(sessionFactory);
            credentialsRepository = new CredentialsRepository(sessionFactory);
            functionRepository = new FunctionRepository(sessionFactory);
            functionResourceRepository = new FunctionResourceRepository(sessionFactory);
            logRepository = new LogRepository(sessionFactory);
            metricRepository = new MetricRepository(sessionFactory);
            metricTypeRepository = new MetricTypeRepository(sessionFactory);
            metricValueRepository = new MetricValueRepository(sessionFactory);
            regionRepository = new RegionRepository(sessionFactory);
            reservationRepository = new ReservationRepository(sessionFactory);
            reservationLogRepository = new ReservationLogRepository(sessionFactory);
            resourceRepository = new ResourceRepository(sessionFactory);
            resourceProviderRepository = new ResourceProviderRepository(sessionFactory);
            resourceReservationRepository = new ResourceReservationRepository(sessionFactory);
            statusRepository = new ResourceReservationStatusRepository(sessionFactory);
            resourceTypeRepository = new ResourceTypeRepository(sessionFactory);
            resourceTypeMetricRepository = new ResourceTypeMetricRepository(sessionFactory);
            runtimeRepository = new RuntimeRepository(sessionFactory);
            vpcRepository = new VPCRepository(sessionFactory);
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupServices);
    }

    private Completable setupEventBus() {
        Maybe<Void> setupEventBus = Maybe.create(emitter -> {
            ServiceBinder serviceBinder = new ServiceBinder(vertx.getDelegate());
            ServiceProxyBinder serviceProxyBinder = new ServiceProxyBinder(serviceBinder);

            serviceProxyBinder.bind(AccountService.class, new AccountServiceImpl(accountRepository));
            serviceProxyBinder.bind(AccountCredentialsService.class,
                new AccountCredentialsServiceImpl(accountCredentialsRepository));
            serviceProxyBinder.bind(CredentialsService.class, new CredentialsServiceImpl(credentialsRepository));
            serviceProxyBinder.bind(FunctionService.class, new FunctionServiceImpl(functionRepository));
            serviceProxyBinder.bind(FunctionResourceService.class,
                new FunctionResourceServiceImpl(functionResourceRepository));
            serviceProxyBinder.bind(LogService.class, new LogServiceImpl(logRepository));
            serviceProxyBinder.bind(MetricService.class, new MetricServiceImpl(metricRepository));
            serviceProxyBinder.bind(MetricTypeService.class, new MetricTypeServiceImpl(metricTypeRepository));
            serviceProxyBinder.bind(MetricValueService.class, new MetricValueServiceImpl(metricValueRepository));
            serviceProxyBinder.bind(RegionService.class, new RegionServiceImpl(regionRepository));
            serviceProxyBinder.bind(ReservationService.class, new ReservationServiceImpl(reservationRepository));
            serviceProxyBinder.bind(ReservationLogService.class,
                new ReservationLogServiceImpl(reservationLogRepository));
            serviceProxyBinder.bind(ResourceService.class, new ResourceServiceImpl(resourceRepository));
            serviceProxyBinder.bind(ResourceProviderService.class,
                new ResourceProviderServiceImpl(resourceProviderRepository));
            serviceProxyBinder.bind(ResourceReservationService.class,
                new ResourceReservationServiceImpl(resourceReservationRepository));
            serviceProxyBinder.bind(ResourceReservationStatusService.class,
                new ResourceReservationStatusServiceImpl(statusRepository));
            serviceProxyBinder.bind(ResourceTypeService.class, new ResourceTypeServiceImpl(resourceTypeRepository));
            serviceProxyBinder.bind(ResourceTypeMetricService.class,
                new ResourceTypeMetricServiceImpl(resourceTypeMetricRepository));
            serviceProxyBinder.bind(RuntimeService.class, new RuntimeServiceImpl(runtimeRepository));
            serviceProxyBinder.bind(VPCService.class, new VPCServiceImpl(vpcRepository));
            serviceProxyBinder.bind(FilePathService.class, new FilePathServiceImpl(vertx.getDelegate()));
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupEventBus);
    }
}
