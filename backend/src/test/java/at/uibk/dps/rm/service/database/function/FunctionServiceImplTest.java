package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.repository.FunctionRepository;
import at.uibk.dps.rm.testutil.TestObjectProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionServiceImplTest {

    private FunctionService functionService;

    @Mock
    FunctionRepository functionRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        functionService = new FunctionServiceImpl(functionRepository);
    }

    @Test
    void findEntityExists(VertxTestContext testContext) {
        long functionId = 1L;
        Function entity = TestObjectProvider.createFunction(functionId, "true");
        CompletionStage<Function> completionStage = CompletionStages.completedFuture(entity);

        when(functionRepository.findByIdAndFetch(functionId)).thenReturn(completionStage);

        functionService.findOne(functionId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("function_id")).isEqualTo(1L);
                assertThat(result.getString("code")).isEqualTo("true");
                testContext.completeNow();
            })));
    }

    @Test
    void findEntityNotExists(VertxTestContext testContext) {
        long functionId = 1L;
        CompletionStage<Function> completionStage = CompletionStages.completedFuture(null);

        when(functionRepository.findByIdAndFetch(functionId)).thenReturn(completionStage);

        functionService.findOne(functionId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        Function f1 = TestObjectProvider.createFunction(1L, "true");
        Function f2 = TestObjectProvider.createFunction(2L, "false");
        CompletionStage<List<Function>> completionStage = CompletionStages.completedFuture(List.of(f1, f2));

        when(functionRepository.findAllAndFetch()).thenReturn(completionStage);

        functionService.findAll()
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("function_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("function_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }
}
