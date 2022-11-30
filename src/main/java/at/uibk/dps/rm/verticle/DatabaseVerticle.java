package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.repository.*;
import at.uibk.dps.rm.service.database.account.*;
import at.uibk.dps.rm.service.database.cloudprovider.CloudProviderService;
import at.uibk.dps.rm.service.database.cloudprovider.CloudProviderServiceImpl;
import at.uibk.dps.rm.service.database.metric.*;
import at.uibk.dps.rm.service.database.reservation.ReservationService;
import at.uibk.dps.rm.service.database.reservation.ReservationServiceImpl;
import at.uibk.dps.rm.service.database.reservation.ResourceReservationService;
import at.uibk.dps.rm.service.database.reservation.ResourceReservationServiceImpl;
import at.uibk.dps.rm.service.database.resource.ResourceService;
import at.uibk.dps.rm.service.database.resource.ResourceServiceImpl;
import at.uibk.dps.rm.service.database.resource.ResourceTypeService;
import at.uibk.dps.rm.service.database.resource.ResourceTypeServiceImpl;
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
    private CloudProviderRepository cloudProviderRepository;
    private CredentialsRepository credentialsRepository;
    private ResourceTypeRepository resourceTypeRepository;
    private ResourceRepository resourceRepository;
    private MetricRepository metricRepository;
    private MetricValueRepository metricValueRepository;
    private MetricTypeRepository metricTypeRepository;
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
                cloudProviderRepository = new CloudProviderRepository(sessionFactory);
                credentialsRepository = new CredentialsRepository(sessionFactory);
                resourceTypeRepository = new ResourceTypeRepository(sessionFactory);
                resourceRepository = new ResourceRepository(sessionFactory);
                metricRepository = new MetricRepository(sessionFactory);
                metricValueRepository = new MetricValueRepository(sessionFactory);
                metricTypeRepository = new MetricTypeRepository(sessionFactory);
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
            new ServiceBinder(vertx.getDelegate())
                .setAddress("account-credentials-service-address")
                .register(AccountCredentialsService.class, accountCredentialsService);

            AccountService accountService =
                new AccountServiceImpl(accountRepository);
            new ServiceBinder(vertx.getDelegate())
                .setAddress("account-service-address")
                .register(AccountService.class, accountService);

            CloudProviderService cloudProviderService =
                new CloudProviderServiceImpl(cloudProviderRepository);
            new ServiceBinder(vertx.getDelegate())
                .setAddress("cloud-provider-service-address")
                .register(CloudProviderService.class, cloudProviderService);

            CredentialsService credentialsService =
                new CredentialsServiceImpl(credentialsRepository);
            new ServiceBinder(vertx.getDelegate())
                .setAddress("credentials-service-address")
                .register(CredentialsService.class, credentialsService);

            ResourceTypeService resourceTypeService =
                new ResourceTypeServiceImpl(resourceTypeRepository);
            new ServiceBinder(vertx.getDelegate())
                .setAddress("resource-type-service-address")
                .register(ResourceTypeService.class, resourceTypeService);

            ResourceService resourceService =
                new ResourceServiceImpl(resourceRepository);
            new ServiceBinder(vertx.getDelegate())
                .setAddress("resource-service-address")
                .register(ResourceService.class, resourceService);

            MetricService metricService =
                new MetricServiceImpl(metricRepository);
            new ServiceBinder(vertx.getDelegate())
                .setAddress("metric-service-address")
                .register(MetricService.class, metricService);

            MetricValueService metricValueService =
                    new MetricValueServiceImpl(metricValueRepository);
            new ServiceBinder(vertx.getDelegate())
                    .setAddress("metric-value-service-address")
                    .register(MetricValueService.class, metricValueService);

            MetricTypeService metricTypeService =
                    new MetricTypeServiceImpl(metricTypeRepository);
            new ServiceBinder(vertx.getDelegate())
                    .setAddress("metric-type-service-address")
                    .register(MetricTypeService.class, metricTypeService);

            ReservationService reservationService =
                    new ReservationServiceImpl(reservationRepository);
            new ServiceBinder(vertx.getDelegate())
                    .setAddress("reservation-service-address")
                    .register(ReservationService.class, reservationService);

            ResourceReservationService resourceReservationService =
                    new ResourceReservationServiceImpl(resourceReservationRepository);
            new ServiceBinder(vertx.getDelegate())
                    .setAddress("resource-reservation-service-address")
                    .register(ResourceReservationService.class, resourceReservationService);
            emitter.onComplete();
        });
        return Completable.fromMaybe(setupEventBus);
    }
}
