package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationStatusService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestReservationProvider;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceReservationStatusCheckerTest {

    private ResourceReservationStatusChecker statusChecker;

    @Mock
    private ResourceReservationStatusService statusService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        statusChecker = new ResourceReservationStatusChecker(statusService);
    }

    @Test
    void checkFindOneByStatusValue(VertxTestContext testContext) {
        String statusValue = ReservationStatusValue.NEW.name();
        JsonObject statusValueJson = JsonObject.mapFrom(TestReservationProvider.createResourceReservationStatusNew());

        when(statusService.findOneByStatusValue(statusValue)).thenReturn(Single.just(statusValueJson));

        statusChecker.checkFindOneByStatusValue(statusValue)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getString("status_value")).isEqualTo("NEW");
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindOneByStatusValueNotFound(VertxTestContext testContext) {
        String statusValue = ReservationStatusValue.NEW.name();
        Single<JsonObject> handler = SingleHelper.getEmptySingle();

        when(statusService.findOneByStatusValue(statusValue)).thenReturn(handler);

        statusChecker.checkFindOneByStatusValue(statusValue)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                }));
    }
}
