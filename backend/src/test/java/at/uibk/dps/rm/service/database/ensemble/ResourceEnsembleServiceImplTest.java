package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.mockprovider.EnsembleRepositoryProviderMock;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.misc.ServiceLevelObjectiveMapper;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import at.uibk.dps.rm.util.validation.SLOCompareUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ResourceEnsembleServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceEnsembleServiceImplTest {

    private ResourceEnsembleService resourceEnsembleService;

    private final EnsembleRepositoryProviderMock repositoryMock = new EnsembleRepositoryProviderMock();

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    private long accountId;
    private Ensemble e1;
    private Resource r1;
    private EnsembleSLO eSlo1;
    private ServiceLevelObjective slo1;
    private ResourceEnsemble re1;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        repositoryMock.mock();
        resourceEnsembleService = new ResourceEnsembleServiceImpl(repositoryMock.getRepositoryProvider(), smProvider);
        accountId = 1L;
        e1 = TestEnsembleProvider.createEnsemble(1L, accountId);
        r1 = TestResourceProvider.createResource(3L);
        eSlo1 = TestEnsembleProvider.createEnsembleSLOGT(5L, "availability", e1.getEnsembleId(),
            0.95);
        SLOValue slo1Value = TestDTOProvider.createSLOValue(0.95);
        slo1 = new ServiceLevelObjective(eSlo1.getName(), eSlo1.getExpression(), List.of(slo1Value));
        re1 = TestEnsembleProvider.createResourceEnsemble(6L, e1, r1);
    }

    // TODO: fix
    @Test
    void saveByEnsembleIdAndResourceId(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getResourceEnsembleRepository().findByEnsembleIdAndResourceId(sessionManager,
            accountId, e1.getEnsembleId(), r1.getResourceId())).thenReturn(Maybe.empty());
        when(repositoryMock.getEnsembleRepository().findByIdAndAccountId(sessionManager, e1.getEnsembleId(), accountId))
            .thenReturn(Maybe.just(e1));
        when(repositoryMock.getResourceRepository().findByIdAndFetch(sessionManager, r1.getResourceId()))
            .thenReturn(Maybe.just(r1));
        when(repositoryMock.getEnsembleSLORepository().findAllByEnsembleId(sessionManager, e1.getEnsembleId()))
            .thenReturn(Single.just(List.of(eSlo1)));
        try(MockedStatic<SLOCompareUtility> mockCompare = Mockito.mockStatic(SLOCompareUtility.class);
                MockedStatic<ServiceLevelObjectiveMapper> mockSLOMapper = Mockito
                    .mockStatic(ServiceLevelObjectiveMapper.class)) {
            mockSLOMapper.when(() -> ServiceLevelObjectiveMapper.mapEnsembleSLO(eSlo1)).thenReturn(slo1);
            /*mockCompare.when(() ->  SLOCompareUtility.resourceFilterBySLOValueType(r1, List.of(slo1)))
                .thenReturn(true);
            mockCompare.when(() -> SLOCompareUtility.resourceValidByNonMetricSLOS(r1, e1))
                .thenReturn(true);*/
            when(sessionManager.persist(argThat((ResourceEnsemble re) ->
                    re.getEnsemble().equals(e1) && re.getResource().equals(r1))))
                .thenReturn(Single.just(re1));
            resourceEnsembleService.saveByEnsembleIdAndResourceId(accountId, e1.getEnsembleId(), r1.getResourceId(),
                testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.getLong("ensemble_id")).isEqualTo(1L);
                    assertThat(result.getLong("resource_id")).isEqualTo(3L);
                    testContext.completeNow();
                })));
        }
    }

    // TODO: fix
    @ParameterizedTest
    @CsvSource({
        "false, true",
        "true, false",
        "false, false"
    })
    void saveByEnsembleIdAndResourceIdSLOMismatch(boolean validMetrics, boolean validNonMetrics,
            VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getResourceEnsembleRepository().findByEnsembleIdAndResourceId(sessionManager,
            accountId, e1.getEnsembleId(), r1.getResourceId())).thenReturn(Maybe.empty());
        when(repositoryMock.getEnsembleRepository().findByIdAndAccountId(sessionManager, e1.getEnsembleId(), accountId))
            .thenReturn(Maybe.just(e1));
        when(repositoryMock.getResourceRepository().findByIdAndFetch(sessionManager, r1.getResourceId()))
            .thenReturn(Maybe.just(r1));
        when(repositoryMock.getEnsembleSLORepository().findAllByEnsembleId(sessionManager, e1.getEnsembleId()))
            .thenReturn(Single.just(List.of(eSlo1)));
        try(MockedStatic<SLOCompareUtility> mockCompare = Mockito.mockStatic(SLOCompareUtility.class);
                MockedStatic<ServiceLevelObjectiveMapper> mockSLOMapper = Mockito
                    .mockStatic(ServiceLevelObjectiveMapper.class)) {
            mockSLOMapper.when(() -> ServiceLevelObjectiveMapper.mapEnsembleSLO(eSlo1)).thenReturn(slo1);
            /*
            mockCompare.when(() ->  SLOCompareUtility.resourceFilterBySLOValueType(r1, List.of(slo1)))
                .thenReturn(validMetrics);
            mockCompare.when(() -> SLOCompareUtility.resourceValidByNonMetricSLOS(r1, e1))
                .thenReturn(validNonMetrics);*/
            resourceEnsembleService.saveByEnsembleIdAndResourceId(accountId, e1.getEnsembleId(), r1.getResourceId(),
                testContext.failing(throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    assertThat(throwable.getMessage())
                        .isEqualTo("resource does not fulfill service level objectives");
                    testContext.completeNow();
                })));
        }
    }

    @Test
    void saveByEnsembleIdAndResourceIdResourceNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getResourceEnsembleRepository().findByEnsembleIdAndResourceId(sessionManager,
            accountId, e1.getEnsembleId(), r1.getResourceId())).thenReturn(Maybe.empty());
        when(repositoryMock.getEnsembleRepository().findByIdAndAccountId(sessionManager, e1.getEnsembleId(), accountId))
            .thenReturn(Maybe.just(e1));
        when(repositoryMock.getResourceRepository().findByIdAndFetch(sessionManager, r1.getResourceId()))
            .thenReturn(Maybe.empty());
        resourceEnsembleService.saveByEnsembleIdAndResourceId(accountId, e1.getEnsembleId(), r1.getResourceId(),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void saveByEnsembleIdAndResourceIdEnsembleNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getResourceEnsembleRepository().findByEnsembleIdAndResourceId(sessionManager,
            accountId, e1.getEnsembleId(), r1.getResourceId())).thenReturn(Maybe.empty());
        when(repositoryMock.getEnsembleRepository().findByIdAndAccountId(sessionManager, e1.getEnsembleId(), accountId))
            .thenReturn(Maybe.empty());
        resourceEnsembleService.saveByEnsembleIdAndResourceId(accountId, e1.getEnsembleId(), r1.getResourceId(),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void saveByEnsembleIdAndResourceIdAlreadyExists(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getResourceEnsembleRepository().findByEnsembleIdAndResourceId(sessionManager,
            accountId, e1.getEnsembleId(), r1.getResourceId())).thenReturn(Maybe.just(re1));
        when(repositoryMock.getEnsembleRepository().findByIdAndAccountId(sessionManager, e1.getEnsembleId(), accountId))
            .thenReturn(Maybe.just(e1));
        resourceEnsembleService.saveByEnsembleIdAndResourceId(accountId, e1.getEnsembleId(), r1.getResourceId(),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void deleteByEnsembleIdAndResourceId(VertxTestContext testContext) {
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(repositoryMock.getResourceEnsembleRepository().findByEnsembleIdAndResourceId(sessionManager, accountId,
            e1.getEnsembleId(), r1.getResourceId())).thenReturn(Maybe.just(re1));
        when(sessionManager.remove(re1)).thenReturn(Completable.complete());
        resourceEnsembleService.deleteByEnsembleIdAndResourceId(accountId, e1.getEnsembleId(), r1.getResourceId(),
            testContext.succeeding(result -> testContext.verify(testContext::completeNow)));
    }

    @Test
    void deleteByEnsembleIdAndResourceIdNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(repositoryMock.getResourceEnsembleRepository().findByEnsembleIdAndResourceId(sessionManager, accountId,
            e1.getEnsembleId(), r1.getResourceId())).thenReturn(Maybe.empty());
        resourceEnsembleService.deleteByEnsembleIdAndResourceId(accountId, e1.getEnsembleId(), r1.getResourceId(),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }
}
