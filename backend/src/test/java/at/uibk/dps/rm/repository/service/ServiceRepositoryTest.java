package at.uibk.dps.rm.repository.service;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestServiceProvider;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceRepositoryTest extends DatabaseTest {

    private final ServiceRepository repository = new ServiceRepository();

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            K8sServiceType k8sStNodePort = TestServiceProvider.createK8sServiceType(1L);
            K8sServiceType k8sStNoSvc = TestServiceProvider.createK8sServiceType(3L);
            ServiceType st1 = TestServiceProvider.createServiceTyp(2L, "notype");
            ServiceType st2 = TestServiceProvider.createServiceTyp(null, "stnew");
            Service s1 = TestServiceProvider.createService(null, st1, "soo1", "latest", k8sStNodePort,
                List.of("80:8080"), accountAdmin, 2, BigDecimal.valueOf(2), 1024,
                List.of(), List.of(), true);
            Service s2 = TestServiceProvider.createService(null, st1, "soo2", "latest", k8sStNoSvc,
                List.of(), accountAdmin, 2, BigDecimal.valueOf(2), 1024,
                List.of(), List.of(), true);
            Service s3 = TestServiceProvider.createService(null, st1, "soo3", "latest", k8sStNodePort,
                List.of("80:8080"), accountDefault, 2, BigDecimal.valueOf(2), 1024,
                List.of(), List.of(), false);
            Service s4 = TestServiceProvider.createService(null, st2, "soo4", "latest", k8sStNoSvc,
                List.of(), accountDefault, 2, BigDecimal.valueOf(2), 1024,
                List.of(), List.of(), true);
            return sessionManager.persist(st2)
                .flatMap(res -> sessionManager.persist(s1))
                .flatMap(res -> sessionManager.persist(s2))
                .flatMap(res -> sessionManager.persist(s3))
                .flatMap(res -> sessionManager.persist(s4));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @ParameterizedTest
    @CsvSource({
        "soo1, 2, 1, true, 1",
        "soo10, 2, 1, false, -1",
        "soo1, 1, 1, false, -1",
        "soo1, 2, 2, false, -1",
        "soo4, 3, 2, true, 4",
        "soo4, 2, 1, false, -1"
    })
    void findOneByNameTypeRuntimeAndCreator(String name, long typeId, long accountId, boolean exists,
             long id, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findOneByNameTypeAndCreator(sessionManager, name, typeId, accountId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getServiceId()).isEqualTo(id);
                    testContext.completeNow();
                } else {
                    testContext.failNow("method did not throw exception");
                }
            }), throwable -> {
                assertThat(exists).isEqualTo(false);
                assertThat(throwable.getCause()).isInstanceOf(NoSuchElementException.class);
                testContext.completeNow();
            });
    }

    @ParameterizedTest
    @CsvSource({
        "1, true, notype, NodePort",
        "2, true, notype, NoService",
        "3, true, notype, NodePort",
        "4, true, stnew, NoService",
        "5, false, none, none",
    })
    void findByIdAndFetch(long serviceId, boolean exists, String serviceType, String k8sServiceType,
            VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findByIdAndFetch(sessionManager, serviceId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getServiceId()).isEqualTo(serviceId);
                    assertThat(result.getServiceType().getName()).isEqualTo(serviceType);
                    assertThat(result.getK8sServiceType().getName()).isEqualTo(k8sServiceType);
                    testContext.completeNow();
                } else {
                    testContext.failNow("method did not throw exception");
                }
            }), throwable -> {
                assertThat(exists).isEqualTo(false);
                assertThat(throwable.getCause()).isInstanceOf(NoSuchElementException.class);
                testContext.completeNow();
            });
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1, true, true, notype, NodePort, user1",
        "1, 2, true, true, notype, NodePort, user1",
        "3, 1, true, false, none, none, none",
        "3, 2, true, true, notype, NodePort, user2",
        "1, 3, true, true, notype, NodePort, user1",
        "5, 1, true, false, none, none, none",
    })
    void findByIdAndAccountId(long serviceId, long accountId, boolean includePublic, boolean exists, String serviceType,
                              String k8sServiceType, String username, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByIdAndAccountId(sessionManager, serviceId, accountId, includePublic))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getServiceId()).isEqualTo(serviceId);
                    assertThat(result.getServiceType().getName()).isEqualTo(serviceType);
                    assertThat(result.getK8sServiceType().getName()).isEqualTo(k8sServiceType);
                    assertThat(result.getCreatedBy().getUsername()).isEqualTo(username);
                    testContext.completeNow();
                } else {
                    testContext.failNow("method did not throw exception");
                }
            }), throwable -> {
                assertThat(exists).isEqualTo(false);
                assertThat(throwable.getCause()).isInstanceOf(NoSuchElementException.class);
                testContext.completeNow();
            });
    }

    @ParameterizedTest
    @ValueSource(strings = {"empty", "nonempty"})
    void findAllByIds(String type, VertxTestContext testContext) {
        Set<Long> serviceIds = type.equals("empty") ? Set.of() : Set.of(1L, 3L, 43L);

        smProvider.withTransactionSingle(sessionManager -> repository.findAllByIds(sessionManager, serviceIds))
            .subscribe(result -> testContext.verify(() -> {
                if (type.equals("empty")) {
                    assertThat(result.size()).isEqualTo(0);
                } else {
                    assertThat(result.get(0).getServiceId()).isEqualTo(1L);
                    assertThat(result.get(1).getServiceId()).isEqualTo(3L);
                }
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @Test
    void findAllAndFetch(VertxTestContext testContext) {
        smProvider.withTransactionSingle(repository::findAllAndFetch)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(4);
                assertThat(result.get(0).getServiceId()).isEqualTo(1L);
                assertThat(result.get(0).getServiceType().getName()).isEqualTo("notype");
                assertThat(result.get(1).getServiceId()).isEqualTo(2L);
                assertThat(result.get(1).getServiceType().getName()).isEqualTo("notype");
                assertThat(result.get(2).getServiceId()).isEqualTo(3L);
                assertThat(result.get(2).getServiceType().getName()).isEqualTo("notype");
                assertThat(result.get(3).getServiceId()).isEqualTo(4L);
                assertThat(result.get(3).getServiceType().getName()).isEqualTo("stnew");
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @Test
    void findAllAccessibleAndFetch(VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllAccessibleAndFetch(sessionManager, 1L))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.get(0).getServiceId()).isEqualTo(1L);
                assertThat(result.get(0).getServiceType().getName()).isEqualTo("notype");
                assertThat(result.get(1).getServiceId()).isEqualTo(2L);
                assertThat(result.get(1).getServiceType().getName()).isEqualTo("notype");
                assertThat(result.get(2).getServiceId()).isEqualTo(4L);
                assertThat(result.get(2).getServiceType().getName()).isEqualTo("stnew");
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @Test
    void findAllByAccountId(VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllByAccountId(sessionManager, 1L))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.get(0).getServiceId()).isEqualTo(1L);
                assertThat(result.get(0).getServiceType().getName()).isEqualTo("notype");
                assertThat(result.get(1).getServiceId()).isEqualTo(2L);
                assertThat(result.get(1).getServiceType().getName()).isEqualTo("notype");
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
