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

            AccountService accountService = new AccountServiceImpl(accountRepository);
            serviceBinder
                .setAddress("account-service-address")
                .register(AccountService.class, accountService);

            AccountCredentialsService accountCredentialsService =
                new AccountCredentialsServiceImpl(accountCredentialsRepository);
            serviceBinder
                .setAddress("account-credentials-service-address")
                .register(AccountCredentialsService.class, accountCredentialsService);

            CredentialsService credentialsService = new CredentialsServiceImpl(credentialsRepository);
            serviceBinder
                .setAddress("credentials-service-address")
                .register(CredentialsService.class, credentialsService);

            FunctionService functionService = new FunctionServiceImpl(functionRepository);
            serviceBinder
                .setAddress("function-service-address")
                .register(FunctionService.class, functionService);

            FunctionResourceService functionResourceService =
                new FunctionResourceServiceImpl(functionResourceRepository);
            serviceBinder
                .setAddress("function-resource-service-address")
                .register(FunctionResourceService.class, functionResourceService);

            LogService logService =
                new LogServiceImpl(logRepository);
            serviceBinder
                .setAddress("log-service-address")
                .register(LogService.class, logService);

            MetricService metricService = new MetricServiceImpl(metricRepository);
            serviceBinder
                .setAddress("metric-service-address")
                .register(MetricService.class, metricService);

            MetricTypeService metricTypeService = new MetricTypeServiceImpl(metricTypeRepository);
            serviceBinder
                .setAddress("metric-type-service-address")
                .register(MetricTypeService.class, metricTypeService);

            MetricValueService metricValueService = new MetricValueServiceImpl(metricValueRepository);
            serviceBinder
                .setAddress("metric-value-service-address")
                .register(MetricValueService.class, metricValueService);

            RegionService regionService =
                new RegionServiceImpl(regionRepository);
            serviceBinder
                .setAddress("region-service-address")
                .register(RegionService.class, regionService);

            ReservationService reservationService = new ReservationServiceImpl(reservationRepository);
            serviceBinder
                .setAddress("reservation-service-address")
                .register(ReservationService.class, reservationService);

            ReservationLogService reservationLogService = new ReservationLogServiceImpl(reservationLogRepository);
            serviceBinder
                .setAddress("reservation-log-service-address")
                .register(ReservationLogService.class, reservationLogService);

            ResourceService resourceService = new ResourceServiceImpl(resourceRepository);
            serviceBinder
                .setAddress("resource-service-address")
                .register(ResourceService.class, resourceService);

            ResourceProviderService resourceProviderService =
                new ResourceProviderServiceImpl(resourceProviderRepository);
            serviceBinder
                .setAddress("resource-provider-service-address")
                .register(ResourceProviderService.class, resourceProviderService);

            ResourceReservationService resourceReservationService =
                new ResourceReservationServiceImpl(resourceReservationRepository);
            serviceBinder
                .setAddress("resource-reservation-service-address")
                .register(ResourceReservationService.class, resourceReservationService);

            ResourceReservationStatusService statusService =
                new ResourceReservationStatusServiceImpl(statusRepository);
            serviceBinder
                .setAddress("resource-reservation-status-service-address")
                .register(ResourceReservationStatusService.class, statusService);

            ResourceTypeService resourceTypeService = new ResourceTypeServiceImpl(resourceTypeRepository);
            serviceBinder
                .setAddress("resource-type-service-address")
                .register(ResourceTypeService.class, resourceTypeService);

            ResourceTypeMetricService resourceTypeMetricService =
                new ResourceTypeMetricServiceImpl(resourceTypeMetricRepository);
            serviceBinder
                .setAddress("resource-type-metric-service-address")
                .register(ResourceTypeMetricService.class, resourceTypeMetricService);

            RuntimeService runtimeService = new RuntimeServiceImpl(runtimeRepository);
            serviceBinder
                .setAddress("runtime-service-address")
                .register(RuntimeService.class, runtimeService);

            VPCService vpcService = new VPCServiceImpl(vpcRepository);
            serviceBinder
                .setAddress("vpc-service-address")
                .register(VPCService.class, vpcService);

            FilePathService filePathService = new FilePathServiceImpl(vertx.getDelegate());
            serviceBinder
                    .setAddress("file-path-service-address")
                    .register(FilePathService.class, filePathService);
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupEventBus);
    }
}
