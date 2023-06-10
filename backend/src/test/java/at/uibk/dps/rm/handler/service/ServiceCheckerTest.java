package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.service.ServiceService;
import at.uibk.dps.rm.testutil.objectprovider.TestServiceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ServiceChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceCheckerTest {


    private ServiceChecker serviceChecker;

    @Mock
    private ServiceService serviceService;



    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        serviceChecker = new ServiceChecker(serviceService);
    }

    @Test
    void checkForDuplicateEntityFalse(VertxTestContext testContext) {
        String name = "svc";
        Service service = TestServiceProvider.createService(1L, name);

        when(serviceService.existsOneByName(name)).thenReturn(Single.just(false));

        serviceChecker.checkForDuplicateEntity(JsonObject.mapFrom(service))
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void checkForDuplicateEntityTrue(VertxTestContext testContext) {
        String name = "svc";
        Service service = TestServiceProvider.createService(1L, name);

        when(serviceService.existsOneByName(name)).thenReturn(Single.just(true));

        serviceChecker.checkForDuplicateEntity(JsonObject.mapFrom(service))
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkExistAllByIds(VertxTestContext testContext) {
        ServiceResourceIds sr1 = TestServiceProvider.createServiceResourceIds(1L, 1L);
        ServiceResourceIds sr2 = TestServiceProvider.createServiceResourceIds(2L, 1L);
        ServiceResourceIds sr3 = TestServiceProvider.createServiceResourceIds(3L, 2L);

        when(serviceService.existsAllByIds(Set.of(1L, 2L, 3L))).thenReturn(Single.just(true));

        serviceChecker.checkExistAllByIds(List.of(sr1, sr2, sr3))
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void checkExistAllByIdsNotExists(VertxTestContext testContext) {
        ServiceResourceIds sr1 = TestServiceProvider.createServiceResourceIds(1L, 1L);
        ServiceResourceIds sr2 = TestServiceProvider.createServiceResourceIds(2L, 1L);
        ServiceResourceIds sr3 = TestServiceProvider.createServiceResourceIds(3L, 2L);

        when(serviceService.existsAllByIds(Set.of(1L, 2L, 3L))).thenReturn(Single.just(false));

        serviceChecker.checkExistAllByIds(List.of(sr1, sr2, sr3))
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
