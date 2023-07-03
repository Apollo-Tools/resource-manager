package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link EnsembleServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class EnsembleServiceImplTest {

    private EnsembleService ensembleService;

    @Mock
    private EnsembleRepository ensembleRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        ensembleService = new EnsembleServiceImpl(ensembleRepository, resourceRepository, sessionFactory);
    }

    @Test
    void findAllByAccountId(VertxTestContext testContext) {
        long accountId = 1L;
        Ensemble e1 = TestEnsembleProvider.createEnsemble(1L, accountId);
        Ensemble e2 = TestEnsembleProvider.createEnsemble(2L, accountId);
        CompletionStage<List<Ensemble>> completionStage = CompletionStages.completedFuture(List.of(e1, e2));

        when(ensembleRepository.findAllByAccountId(session, accountId)).thenReturn(completionStage);

        ensembleService.findAllByAccountId(accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                for(int i=0; i<2; i++) {
                    assertThat(result.getJsonObject(i).getLong("ensemble_id")).isEqualTo(i+1);
                    assertThat(result.getJsonObject(i).getString("name")).isEqualTo("ensemble" + (i+1));
                    assertThat(result.getJsonObject(i).getValue("created_by")).isNull();
                    assertThat(result.getJsonObject(i).getValue("resource_types")).isNull();
                    assertThat(result.getJsonObject(i).getValue("regions")).isNull();
                    assertThat(result.getJsonObject(i).getValue("providers")).isNull();
                }
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByIdAndAccountId(VertxTestContext testContext) {
        long ensembleId = 1L, accountId = 2L;
        Ensemble e1 = TestEnsembleProvider.createEnsemble(ensembleId, accountId);
        CompletionStage<Ensemble> completionStage = CompletionStages.completedFuture(e1);

        when(ensembleRepository.findByIdAndAccountId(session, ensembleId, accountId)).thenReturn(completionStage);

        ensembleService.findOneByIdAndAccountId(ensembleId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("ensemble_id")).isEqualTo(1L);
                assertThat(result.getString("name")).isEqualTo("ensemble" + 1L);
                assertThat(result.getValue("created_by")).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByIdAndAccountIdNotFound(VertxTestContext testContext) {
        long ensembleId = 1L, accountId = 2L;
        CompletionStage<Ensemble> completionStage = CompletionStages.nullFuture();

        when(ensembleRepository.findByIdAndAccountId(session, ensembleId, accountId)).thenReturn(completionStage);

        ensembleService.findOneByIdAndAccountId(ensembleId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    private static Stream<Arguments> provideExistsOneByNameAndAccount() {
        String name = "ensemble";
        long ensembleId = 1L, accountId = 2L;
        Ensemble e1 = TestEnsembleProvider.createEnsemble(ensembleId, accountId, name);

        return Stream.of(
                Arguments.of(name, accountId, CompletionStages.completedFuture(e1), true),
                Arguments.of(name, accountId, CompletionStages.nullFuture(), false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideExistsOneByNameAndAccount")
    void existsOneByNameAndAccountIdTrue(String name, long accountId, CompletionStage<Ensemble> completionStage,
            boolean expected,
            VertxTestContext testContext) {
        when(ensembleRepository.findByNameAndAccountId(session, name, accountId)).thenReturn(completionStage);

        ensembleService.existsOneByNameAndAccountId(name, accountId)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result).isEqualTo(expected);
                    testContext.completeNow();
                })));
    }

    @Test
    void updateEnsembleValidity(VertxTestContext testContext) {
        long ensembleId = 1L;
        boolean isValid = true;
        CompletionStage<Integer> completionStage = CompletionStages.completedFuture(1);

        when(ensembleRepository.updateValidity(session, ensembleId, isValid)).thenReturn(completionStage);

        ensembleService.updateEnsembleValidity(ensembleId, isValid)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result).isNull();
                    testContext.completeNow();
                })));
    }
}
