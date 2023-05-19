package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.repository.function.FunctionResourceRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
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

/**
 * Implements tests for the {@link FunctionResourceServiceImpl} class.
 *
 * @author matthi-g
 */
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
    void findByFunctionAndResourceExists(VertxTestContext testContext) {
        long functionId = 2L, resourceId = 3L;
        FunctionResource entity = TestFunctionProvider.createFunctionResource(1L);
        CompletionStage<FunctionResource> completionStage = CompletionStages.completedFuture(entity);

        when(functionResourceRepository.findByFunctionAndResource(functionId, resourceId)).thenReturn(completionStage);

        functionResourceService.findOneByFunctionAndResource(functionId, resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getJsonObject("function")).isNull();
                assertThat(result.getJsonObject("resource").getJsonObject("resource_type")).isNull();
                assertThat(result.getJsonObject("resource").getJsonObject("metric_values")).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findEntityNotExists(VertxTestContext testContext) {
        long functionId = 2L, resourceId = 3L;
        CompletionStage<FunctionResource> completionStage = CompletionStages.completedFuture(null);

        when(functionResourceRepository.findByFunctionAndResource(functionId, resourceId)).thenReturn(completionStage);

        functionResourceService.findOneByFunctionAndResource(functionId, resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByReservationId(VertxTestContext testContext) {
        long reservationId = 1L;
        Function f1 = TestFunctionProvider.createFunction(22L, "func1", "false");
        Resource r1 = TestResourceProvider.createResource(33L);
        FunctionResource fr1 = TestFunctionProvider.createFunctionResource(1L, f1, r1, false);
        Resource r2 = TestResourceProvider.createResource(44L);
        FunctionResource fr2 = TestFunctionProvider.createFunctionResource(2L, f1, r2, false);
        CompletionStage<List<FunctionResource>> completionStage = CompletionStages.completedFuture(List.of(fr1, fr2));

        when(functionResourceRepository.findAllByReservationIdAndFetch(reservationId)).thenReturn(completionStage);

        functionResourceService.findAllByReservationId(reservationId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("function_resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(0).getJsonObject("resource").getLong("resource_id"))
                    .isEqualTo(33L);
                assertThat(result.getJsonObject(0).getJsonObject("function").getLong("function_id"))
                    .isEqualTo(22L);
                assertThat(result.getJsonObject(1).getLong("function_resource_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(1).getJsonObject("resource").getLong("resource_id"))
                    .isEqualTo(44L);
                assertThat(result.getJsonObject(1).getJsonObject("function").getLong("function_id"))
                    .isEqualTo(22L);
                testContext.completeNow();
            })));
    }


    @Test
    void existsOneFunctionAndResourceTrue(VertxTestContext testContext) {
        long functionResourceId = 1L, functionId = 2L, resourceId = 2L;
        Function function = TestFunctionProvider.createFunction(functionId, "test", "false");
        Resource resource = TestResourceProvider.createResource(resourceId);
        FunctionResource entity = TestFunctionProvider
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
