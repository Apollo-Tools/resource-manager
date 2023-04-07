package at.uibk.dps.rm.handler.metric;


import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.metric.ResourceTypeMetricService;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceTypeMetricCheckerTest {

    private ResourceTypeMetricChecker checker;

    @Mock
    private ResourceTypeMetricService service;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        checker = new ResourceTypeMetricChecker(service);
    }

    @Test
    void checkMissingRequiredResourceTypeMetricsFalse(VertxTestContext testContext) {
        long resourceId = 1L;

        when(service.missingRequiredResourceTypeMetricsByResourceId(resourceId))
            .thenReturn(Single.just(false));

        checker.checkMissingRequiredResourceTypeMetricsByResource(resourceId)
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

        checker.checkMissingRequiredResourceTypeMetricsByResource(resourceId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkMissingRequiredMetricsByFunctionResources(VertxTestContext testContext) {
        FunctionResource fr1 = TestFunctionProvider.createFunctionResource(1L, 1L);
        FunctionResource fr2 = TestFunctionProvider.createFunctionResource(2L, 2L);
        FunctionResource fr3 = TestFunctionProvider.createFunctionResource(3L, 2L);
        List<JsonObject> functionResources = List.of(JsonObject.mapFrom(fr1), JsonObject.mapFrom(fr2),
            JsonObject.mapFrom(fr3));

        when(service.missingRequiredResourceTypeMetricsByResourceId(or(eq(1L), eq(2L))))
            .thenReturn(Single.just(false));

        checker.checkMissingRequiredMetricsByFunctionResources(functionResources)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void checkMissingRequiredMetricsByFunctionResourcesMissingMetric(VertxTestContext testContext) {
        FunctionResource fr1 = TestFunctionProvider.createFunctionResource(1L, 1L);
        FunctionResource fr2 = TestFunctionProvider.createFunctionResource(2L, 2L);
        FunctionResource fr3 = TestFunctionProvider.createFunctionResource(3L, 2L);
        List<JsonObject> functionResources = List.of(JsonObject.mapFrom(fr1), JsonObject.mapFrom(fr2),
            JsonObject.mapFrom(fr3));

        when(service.missingRequiredResourceTypeMetricsByResourceId(1L))
            .thenReturn(Single.just(false));
        when(service.missingRequiredResourceTypeMetricsByResourceId(2L))
            .thenReturn(Single.just(true));

        checker.checkMissingRequiredMetricsByFunctionResources(functionResources)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
