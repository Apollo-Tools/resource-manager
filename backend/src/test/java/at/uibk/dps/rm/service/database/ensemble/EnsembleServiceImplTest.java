package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.mockprovider.EnsembleRepositoryProviderMock;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
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
 * Implements tests for the {@link EnsembleServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class EnsembleServiceImplTest {

    private EnsembleService ensembleService;

    private final EnsembleRepositoryProviderMock repositoryMock = new EnsembleRepositoryProviderMock();

    @Mock
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        repositoryMock.mock();
        ensembleService = new EnsembleServiceImpl(repositoryMock.getRepositoryProvider(), sessionFactory);
    }

    @Test
    void findAllByAccountId(VertxTestContext testContext) {
        long accountId = 1L;
        Ensemble e1 = TestEnsembleProvider.createEnsemble(1L, accountId);
        Ensemble e2 = TestEnsembleProvider.createEnsemble(2L, accountId);
        CompletionStage<List<Ensemble>> completionStage = CompletionStages.completedFuture(List.of(e1, e2));

        SessionMockHelper.mockSession(sessionFactory, session);
        when(repositoryMock.getEnsembleRepository().findAllByAccountId(session, accountId)).thenReturn(completionStage);

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
        Resource r1 = TestResourceProvider.createResource(1L);
        EnsembleSLO slo1 = TestEnsembleProvider.createEnsembleSLO(11L, "availability", ensembleId);
        CompletionStage<Ensemble> cs1 = CompletionStages.completedFuture(e1);
        CompletionStage<List<Resource>> cs2 = CompletionStages.completedFuture(List.of(r1));
        CompletionStage<List<EnsembleSLO>> cs3 = CompletionStages.completedFuture(List.of(slo1));


        SessionMockHelper.mockSession(sessionFactory, session);
        when(repositoryMock.getEnsembleRepository()
            .findByIdAndAccountId(session, ensembleId, accountId)).thenReturn(cs1);
        when(repositoryMock.getResourceRepository().findAllByEnsembleId(session, ensembleId)).thenReturn(cs2);
        when(repositoryMock.getEnsembleSLORepository().findAllByEnsembleId(session, ensembleId)).thenReturn(cs3);

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

        SessionMockHelper.mockSession(sessionFactory, session);
        when(repositoryMock.getEnsembleRepository()
            .findByIdAndAccountId(session, ensembleId, accountId)).thenReturn(completionStage);

        ensembleService.findOneByIdAndAccountId(ensembleId, accountId)
            .onComplete(testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                assertThat(throwable.getMessage()).isEqualTo("Ensemble not found");
                testContext.completeNow();
            })));
    }
}
