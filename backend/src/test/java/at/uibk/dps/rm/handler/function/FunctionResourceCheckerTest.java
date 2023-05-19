package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionResourceService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionResourceCheckerTest {

    private FunctionResourceChecker functionResourceChecker;

    @Mock
    private FunctionResourceService functionResourceService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        functionResourceChecker = new FunctionResourceChecker(functionResourceService);
    }

    @Test
    void checkForDuplicateEntityByFunctionAndResourceNotExists(VertxTestContext testContext) {
        long functionId = 1L, resourceId = 1L;

        when(functionResourceService.existsOneByFunctionAndResource(functionId, resourceId))
            .thenReturn(Single.just(false));

        functionResourceChecker.checkForDuplicateByFunctionAndResource(functionId, resourceId)
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );

        verify(functionResourceService).existsOneByFunctionAndResource(functionId, resourceId);
        testContext.completeNow();
    }

    @Test
    void checkForDuplicateEntityExists(VertxTestContext testContext) {
        long functionId = 1L, resourceId = 1L;

        when(functionResourceService.existsOneByFunctionAndResource(functionId, resourceId))
            .thenReturn(Single.just(true));

        functionResourceChecker.checkForDuplicateByFunctionAndResource(functionId, resourceId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void findOneByFunctionAndResourceExists(VertxTestContext testContext) {
        long functionId = 2L, resourceId = 3L;
        Function function = TestFunctionProvider.createFunction(functionId, "multiply", "false");
        Resource resource = TestResourceProvider.createResource(resourceId);
        FunctionResource functionResource = TestFunctionProvider.createFunctionResource(1L, function, resource, false);

        when(functionResourceService.findOneByFunctionAndResource(functionId, resourceId))
            .thenReturn(Single.just(JsonObject.mapFrom(functionResource)));

        functionResourceChecker.checkFindOneByFunctionAndResource(functionId, resourceId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonObject("function").getLong("function_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject("resource").getLong("resource_id")).isEqualTo(3L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method throw exception")));
    }

    @Test
    void findOneByFunctionAndResourceNotExists(VertxTestContext testContext) {
        long functionId = 1L, resourceId = 1L;
        Single<JsonObject> handler = SingleHelper.getEmptySingle();

        when(functionResourceService.findOneByFunctionAndResource(functionId, resourceId))
            .thenReturn(handler);

        functionResourceChecker.checkFindOneByFunctionAndResource(functionId, resourceId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkFindAllByReservationIdValid(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestAccountProvider.createAccount(1L);
        Reservation reservation = TestReservationProvider.createReservation(reservationId, false, account);
        Resource r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);
        Resource r3 = TestResourceProvider.createResource(3L);
        ResourceReservation resourceReservation1 = TestReservationProvider.createResourceReservation(1L, reservation,
            r1, new ResourceReservationStatus());
        ResourceReservation resourceReservation2 = TestReservationProvider.createResourceReservation(2L, reservation,
            r2, new ResourceReservationStatus());
        ResourceReservation resourceReservation3 = TestReservationProvider.createResourceReservation(3L, reservation,
            r3, new ResourceReservationStatus());
        JsonArray resourceReservations = new JsonArray(List.of(JsonObject.mapFrom(resourceReservation1),
            JsonObject.mapFrom(resourceReservation2), JsonObject.mapFrom(resourceReservation3)));

        when(functionResourceService.findAllByReservationId(reservationId)).thenReturn(Single.just(resourceReservations));

        functionResourceChecker.checkFindAllByReservationId(reservationId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("resource_reservation_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("resource_reservation_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(2).getLong("resource_reservation_id")).isEqualTo(3L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllByReservationIdEmptyList(VertxTestContext testContext) {
        long reservationId = 1L;
        JsonArray resourceReservations = new JsonArray(new ArrayList<JsonObject>());

        when(functionResourceService.findAllByReservationId(reservationId)).thenReturn(Single.just(resourceReservations));

        functionResourceChecker.checkFindAllByReservationId(reservationId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllByReservationIdNotFound(VertxTestContext testContext) {
        long reservationId = 1L;
        Single<JsonArray> handler = SingleHelper.getEmptySingle();

        when(functionResourceService.findAllByReservationId(reservationId)).thenReturn(handler);

        functionResourceChecker.checkFindAllByReservationId(reservationId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkFunctionResourceExistsByFunctionAndResourceExists(VertxTestContext testContext) {
        long functionId = 1L, resourceId = 1L;

        when(functionResourceService.existsOneByFunctionAndResource(functionId, resourceId))
            .thenReturn(Single.just(true));

        functionResourceChecker.checkExistsByFunctionAndResource(functionId, resourceId)
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );

        verify(functionResourceService).existsOneByFunctionAndResource(functionId, resourceId);
        testContext.completeNow();
    }

    @Test
    void checkMetricValueExistsByResourceAndMetricNotExists(VertxTestContext testContext) {
        long functionId = 1L, resourceId = 1L;

        when(functionResourceService.existsOneByFunctionAndResource(functionId, resourceId))
            .thenReturn(Single.just(false));

        functionResourceChecker.checkExistsByFunctionAndResource(functionId, resourceId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
