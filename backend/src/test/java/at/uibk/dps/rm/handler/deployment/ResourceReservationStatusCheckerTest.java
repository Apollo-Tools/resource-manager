package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.deployment.ResourceDeploymentStatusService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
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

/**
 * Implements tests for the {@link ResourceDeploymentStatusChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceReservationStatusCheckerTest {

    private ResourceDeploymentStatusChecker statusChecker;

    @Mock
    private ResourceDeploymentStatusService statusService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        statusChecker = new ResourceDeploymentStatusChecker(statusService);
    }

    @Test
    void checkFindOneByStatusValue(VertxTestContext testContext) {
        String statusValue = DeploymentStatusValue.NEW.name();
        JsonObject statusValueJson = JsonObject.mapFrom(TestDeploymentProvider.createResourceDeploymentStatusNew());

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
        String statusValue = DeploymentStatusValue.NEW.name();
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
