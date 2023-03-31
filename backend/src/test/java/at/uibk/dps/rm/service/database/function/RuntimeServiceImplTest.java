package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.repository.function.RuntimeRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class RuntimeServiceImplTest {

    private RuntimeService runtimeService;

    @Mock
    RuntimeRepository runtimeRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        runtimeService = new RuntimeServiceImpl(runtimeRepository);
    }

    @Test
    void existsOneByNameTrue(VertxTestContext testContext) {
        String name = "python";
        long runtimeId = 1L;
        Runtime entity = TestFunctionProvider.createRuntime(runtimeId, name);
        CompletionStage<Runtime> completionStage = CompletionStages.completedFuture(entity);
        when(runtimeRepository.findByName(name)).thenReturn(completionStage);

        runtimeService.existsOneByName(name)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                testContext.completeNow();
            })));
    }

    @Test
    void existsOneByNameFalse(VertxTestContext testContext) {
        String name = "python";
        CompletionStage<Runtime> completionStage = CompletionStages.completedFuture(null);
        when(runtimeRepository.findByName(name)).thenReturn(completionStage);

        runtimeService.existsOneByName(name)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                testContext.completeNow();
            })));
    }
}
