package at.uibk.dps.rm.handler.metric;


import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.metric.ResourceTypeMetricService;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
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

        checker.checkMissingRequiredResourceTypeMetrics(resourceId)
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

        checker.checkMissingRequiredResourceTypeMetrics(resourceId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
