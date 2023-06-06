package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link PlatformMetricChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceTypeMetricCheckerTest {

    private PlatformMetricChecker checker;

    @Mock
    private ResourceTypeMetricService service;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        checker = new PlatformMetricChecker(service);
    }

    @Test
    void checkMissingRequiredResourceTypeMetricsFalse(VertxTestContext testContext) {
        long resourceId = 1L;

        when(service.missingRequiredResourceTypeMetricsByResourceId(resourceId))
            .thenReturn(Single.just(false));

        checker.checkMissingRequiredPlatformMetricsByResource(resourceId)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void checkMissingRequiredResourceTypeMetricsTrue(VertxTestContext testContext) {
        long resourceId = 1L;

        when(service.missingRequiredResourceTypeMetricsByResourceId(resourceId))
            .thenReturn(Single.just(true));

        checker.checkMissingRequiredPlatformMetricsByResource(resourceId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkMissingRequiredMetricsByFunctionResources(VertxTestContext testContext) {
        JsonObject r1 = JsonObject.mapFrom(TestResourceProvider.createResource(1L));
        JsonObject r2 = JsonObject.mapFrom(TestResourceProvider.createResource(2L));
        JsonObject r3 = JsonObject.mapFrom(TestResourceProvider.createResource(1L));
        JsonArray resources = new JsonArray(List.of(r1, r2, r3));

        when(service.missingRequiredResourceTypeMetricsByResourceId(or(eq(1L), eq(2L))))
            .thenReturn(Single.just(false));

        checker.checkMissingRequiredMetricsByResources(resources)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void checkMissingRequiredMetricsByFunctionResourcesMissingMetric(VertxTestContext testContext) {
        JsonObject r1 = JsonObject.mapFrom(TestResourceProvider.createResource(1L));
        JsonObject r2 = JsonObject.mapFrom(TestResourceProvider.createResource(2L));
        JsonArray resources = new JsonArray(List.of(r1, r2));

        when(service.missingRequiredResourceTypeMetricsByResourceId(1L))
            .thenReturn(Single.just(false));
        when(service.missingRequiredResourceTypeMetricsByResourceId(2L))
            .thenReturn(Single.just(true));

        checker.checkMissingRequiredMetricsByResources(resources)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
