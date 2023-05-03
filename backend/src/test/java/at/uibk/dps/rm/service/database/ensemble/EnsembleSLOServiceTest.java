package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.repository.ensemble.EnsembleSLORepository;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
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

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class EnsembleSLOServiceTest {

    private EnsembleSLOService ensembleSLOService;

    @Mock
    private EnsembleSLORepository ensembleSLORepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        ensembleSLOService = new EnsembleSLOServiceImpl(ensembleSLORepository);
    }

    @Test
    void findAll(VertxTestContext testContext) {
        long ensembleId = 1L;
        EnsembleSLO eslo1 = TestEnsembleProvider.createEnsembleSLO(1L, "slo1", ensembleId);
        EnsembleSLO eslo2 = TestEnsembleProvider.createEnsembleSLO(2L, "slo2", ensembleId);
        CompletionStage<List<EnsembleSLO>> completionStage = CompletionStages.completedFuture(List.of(eslo1, eslo2));

        when(ensembleSLORepository.findAllByEnsembleId(ensembleId)).thenReturn(completionStage);

        ensembleSLOService.findAllByEnsembleId(ensembleId)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("ensemble_slo_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(0).getValue("ensemble")).isNull();
                    assertThat(result.getJsonObject(1).getLong("ensemble_slo_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(1).getValue("ensemble")).isNull();
                    testContext.completeNow();
                })));
    }
}
