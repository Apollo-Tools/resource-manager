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
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import at.uibk.dps.rm.service.database.util.SessionManager;
import org.hibernate.reactive.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
    
    private SessionManager sessionManager;

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
        Single<List<Ensemble>> single = Single.just(List.of(e1, e2));

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(repositoryMock.getEnsembleRepository().findAllByAccountId(sessionManager, accountId)).thenReturn(single);

        ensembleService.findAllByAccountId(accountId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                for(int i=0; i<2; i++) {
                    assertThat(result.getJsonObject(i).getLong("ensemble_id")).isEqualTo(i+1);
                    assertThat(result.getJsonObject(i).getString("name")).isEqualTo("ensemble" + (i+1));
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
        Maybe<Ensemble> maybenEnsemble = Maybe.just(e1);
        Single<List<Resource>> singleResources = Single.just(List.of(r1));
        Single<List<EnsembleSLO>> singleSLOs = Single.just(List.of(slo1));


        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(repositoryMock.getEnsembleRepository()
            .findByIdAndAccountId(sessionManager, ensembleId, accountId)).thenReturn(maybenEnsemble);
        when(repositoryMock.getResourceRepository().findAllByEnsembleId(sessionManager, ensembleId))
            .thenReturn(singleResources);
        when(repositoryMock.getEnsembleSLORepository().findAllByEnsembleId(sessionManager, ensembleId))
            .thenReturn(singleSLOs);

        ensembleService.findOneByIdAndAccountId(ensembleId, accountId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("ensemble_id")).isEqualTo(1L);
                assertThat(result.getString("name")).isEqualTo("ensemble" + 1L);
                assertThat(result.getValue("created_by")).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByIdAndAccountIdNotFound(VertxTestContext testContext) {
        long ensembleId = 1L, accountId = 2L;
        Maybe<Ensemble> maybe = Maybe.empty();

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(repositoryMock.getEnsembleRepository()
            .findByIdAndAccountId(sessionManager, ensembleId, accountId)).thenReturn(maybe);

        ensembleService.findOneByIdAndAccountId(ensembleId, accountId, testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                assertThat(throwable.getMessage()).isEqualTo("Ensemble not found");
                testContext.completeNow();
            })));
    }
}
