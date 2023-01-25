package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.repository.FunctionResourceRepository;
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

import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionResourceServiceImplTest {

    private FunctionResourceService functionResourceService;

    @Mock
    FunctionResourceRepository functionResourceRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        functionResourceService = new FunctionResourceServiceImpl(functionResourceRepository);
    }

    @Test
    void existsOneFunctionAndResourceTrue(VertxTestContext testContext) {
        long functionResourceId = 1L, functionId = 2L, resourceId = 2L;
        Function function = TestObjectProvider.createFunction(functionId, "test", "false");
        Resource resource = TestObjectProvider.createResource(resourceId);
        FunctionResource entity = TestObjectProvider
            .createFunctionResource(functionResourceId, function, resource, false);
        CompletionStage<FunctionResource> completionStage = CompletionStages.completedFuture(entity);

        when(functionResourceRepository.findByFunctionAndResource(functionId, resourceId)).thenReturn(completionStage);

        functionResourceService.existsOneByFunctionAndResource(functionId, resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                testContext.completeNow();
            })));
    }

    @Test
    void existsOneFunctionAndResourceFalse(VertxTestContext testContext) {
        long functionId = 2L, resourceId = 2L;
        CompletionStage<FunctionResource> completionStage = CompletionStages.completedFuture(null);

        when(functionResourceRepository.findByFunctionAndResource(functionId, resourceId)).thenReturn(completionStage);

        functionResourceService.existsOneByFunctionAndResource(functionId, resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                testContext.completeNow();
            })));
    }



    @Test
    void deleteByFunctionAndResource(VertxTestContext testContext) {
        long functionId = 1L, resourceId = 2L;
        CompletionStage<Integer> completionStage = CompletionStages.completedFuture(1);

        when(functionResourceRepository.deleteByFunctionAndResource(functionId, resourceId))
            .thenReturn(completionStage);

        functionResourceService.deleteByFunctionAndResource(functionId, resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }
}
