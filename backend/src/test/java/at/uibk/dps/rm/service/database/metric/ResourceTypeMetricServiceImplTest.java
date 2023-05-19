package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.repository.metric.ResourceTypeMetricRepository;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ResourceTypeMetricServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceTypeMetricServiceImplTest {

    private ResourceTypeMetricService service;

    @Mock
    private ResourceTypeMetricRepository repository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new ResourceTypeMetricServiceImpl(repository);
    }

    @ParameterizedTest
    @CsvSource({
        "3, true",
        "0, false"
    })
    void missingRequiredResourceTypeMetricsByResourceId(long missingRtms, boolean expectedResult,
                                                        VertxTestContext testContext) {
        long resourceId = 1L;
        CompletionStage<Long> completionStage = CompletionStages.completedFuture(missingRtms);
        when(repository.countMissingRequiredMetricValuesByResourceId(resourceId)).thenReturn(completionStage);

        service.missingRequiredResourceTypeMetricsByResourceId(resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(expectedResult);
                testContext.completeNow();
            })));
    }
}
