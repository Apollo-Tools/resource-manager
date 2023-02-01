package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.repository.account.AccountCredentialsRepository;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.repository.function.FunctionResourceRepository;
import at.uibk.dps.rm.repository.function.RuntimeRepository;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.metric.MetricTypeRepository;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.repository.reservation.ReservationRepository;
import at.uibk.dps.rm.repository.reservation.ResourceReservationRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resource.ResourceTypeRepository;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.service.database.account.*;
import at.uibk.dps.rm.service.database.function.*;
import at.uibk.dps.rm.service.database.resourceprovider.ResourceProviderService;
import at.uibk.dps.rm.service.database.resourceprovider.ResourceProviderServiceImpl;
import at.uibk.dps.rm.service.database.metric.*;
import at.uibk.dps.rm.service.database.reservation.ReservationService;
import at.uibk.dps.rm.service.database.reservation.ReservationServiceImpl;
import at.uibk.dps.rm.service.database.reservation.ResourceReservationService;
import at.uibk.dps.rm.service.database.reservation.ResourceReservationServiceImpl;
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

    private AccountCredentialsRepository accountCredentialsRepository;
    private AccountRepository accountRepository;
    private ResourceProviderRepository resourceProviderRepository;
    private CredentialsRepository credentialsRepository;
    private ResourceTypeRepository resourceTypeRepository;
    private ResourceRepository resourceRepository;
    private MetricRepository metricRepository;
    private MetricValueRepository metricValueRepository;
    private MetricTypeRepository metricTypeRepository;
    private RuntimeRepository runtimeRepository;
    private FunctionRepository functionRepository;
    private FunctionResourceRepository functionResourceRepository;
    private ReservationRepository reservationRepository;
    private ResourceReservationRepository resourceReservationRepository;

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
                accountCredentialsRepository = new AccountCredentialsRepository(sessionFactory);
                accountRepository = new AccountRepository(sessionFactory);
                resourceProviderRepository = new ResourceProviderRepository(sessionFactory);
                credentialsRepository = new CredentialsRepository(sessionFactory);
                resourceTypeRepository = new ResourceTypeRepository(sessionFactory);
                resourceRepository = new ResourceRepository(sessionFactory);
                metricRepository = new MetricRepository(sessionFactory);
                metricValueRepository = new MetricValueRepository(sessionFactory);
                metricTypeRepository = new MetricTypeRepository(sessionFactory);
                runtimeRepository = new RuntimeRepository(sessionFactory);
                functionRepository = new FunctionRepository(sessionFactory);
                functionResourceRepository = new FunctionResourceRepository(sessionFactory);
                reservationRepository = new ReservationRepository(sessionFactory);
                resourceReservationRepository = new ResourceReservationRepository(sessionFactory);
                emitter.onComplete();
            }
        );
        return Completable.fromMaybe(setupServices);
    }

    private Completable setupEventBus() {
        Maybe<Void> setupEventBus = Maybe.create(emitter -> {
            AccountCredentialsService accountCredentialsService =
                new AccountCredentialsServiceImpl(accountCredentialsRepository);

            ServiceBinder serviceBinder = new ServiceBinder(vertx.getDelegate());
            serviceBinder
                .setAddress("account-credentials-service-address")
                .register(AccountCredentialsService.class, accountCredentialsService);

            AccountService accountService = new AccountServiceImpl(accountRepository);
            serviceBinder
                .setAddress("account-service-address")
                .register(AccountService.class, accountService);

            ResourceProviderService resourceProviderService =
                new ResourceProviderServiceImpl(resourceProviderRepository);
            serviceBinder
                .setAddress("resource-provider-service-address")
                .register(ResourceProviderService.class, resourceProviderService);

            CredentialsService credentialsService = new CredentialsServiceImpl(credentialsRepository);
            serviceBinder
                .setAddress("credentials-service-address")
                .register(CredentialsService.class, credentialsService);

            ResourceTypeService resourceTypeService = new ResourceTypeServiceImpl(resourceTypeRepository);
            serviceBinder
                .setAddress("resource-type-service-address")
                .register(ResourceTypeService.class, resourceTypeService);

            ResourceService resourceService = new ResourceServiceImpl(resourceRepository);
            serviceBinder
                .setAddress("resource-service-address")
                .register(ResourceService.class, resourceService);

            MetricService metricService = new MetricServiceImpl(metricRepository);
            serviceBinder
                .setAddress("metric-service-address")
                .register(MetricService.class, metricService);

            MetricValueService metricValueService = new MetricValueServiceImpl(metricValueRepository);
            serviceBinder
                    .setAddress("metric-value-service-address")
                    .register(MetricValueService.class, metricValueService);

            MetricTypeService metricTypeService = new MetricTypeServiceImpl(metricTypeRepository);
            serviceBinder
                    .setAddress("metric-type-service-address")
                    .register(MetricTypeService.class, metricTypeService);

            RuntimeService runtimeService = new RuntimeServiceImpl(runtimeRepository);
            serviceBinder
                .setAddress("runtime-service-address")
                .register(RuntimeService.class, runtimeService);

            FunctionService functionService = new FunctionServiceImpl(functionRepository);
            serviceBinder
                .setAddress("function-service-address")
                .register(FunctionService.class, functionService);

            FunctionResourceService functionResourceService =
                new FunctionResourceServiceImpl(functionResourceRepository);
            serviceBinder
                .setAddress("function-resource-service-address")
                .register(FunctionResourceService.class, functionResourceService);

            ReservationService reservationService = new ReservationServiceImpl(reservationRepository);
            serviceBinder
                    .setAddress("reservation-service-address")
                    .register(ReservationService.class, reservationService);

            ResourceReservationService resourceReservationService =
                new ResourceReservationServiceImpl(resourceReservationRepository);
            serviceBinder
                    .setAddress("resource-reservation-service-address")
                    .register(ResourceReservationService.class, resourceReservationService);

            FilePathService filePathService =
                new FilePathServiceImpl(vertx.getDelegate());
            serviceBinder
                    .setAddress("file-path-service-address")
                    .register(FilePathService.class, filePathService);
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupEventBus);
    }
}
