package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.ensemble.ResourceEnsembleStatus;
import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.testutil.mockprovider.DatabaseUtilMockprovider;
import at.uibk.dps.rm.testutil.mockprovider.EnsembleRepositoryProviderMock;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link EnsembleValidationUtilityTest} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class EnsembleValidationUtilityTest {

    private EnsembleValidationUtility validationUtility;

    private final EnsembleRepositoryProviderMock repositoryMock = new EnsembleRepositoryProviderMock();

    @Mock
    private SessionManager sessionManager;

    private long ensembleId, accountId;
    private Resource r1,r2;
    private Ensemble e1;

    @BeforeEach
    void initTest() {
        repositoryMock.mock();
        validationUtility = new EnsembleValidationUtility(repositoryMock.getRepositoryProvider());
        ensembleId = 1L;
        accountId = 2L;
        r1 = TestResourceProvider.createClusterWithoutNodes(1L, "mainresource");
        r2 = TestResourceProvider.createSubResource(2L, "subresource", r1.getMain());
        e1 = TestEnsembleProvider.createEnsemble(ensembleId, accountId, "ensemble8");
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid", "r1invalid", "r2invalid", "bothInvalid"})
    void validateAndUpdateEnsemble(String type, VertxTestContext testContext) {
        ResourceEnsembleStatus r1es = new ResourceEnsembleStatus(r1.getResourceId(),
            type.equals("valid") || type.equals("r2invalid"));
        ResourceEnsembleStatus r2es = new ResourceEnsembleStatus(r2.getResourceId(),
            type.equals("valid") || type.equals("r1invalid"));
        GetOneEnsemble getOneEnsemble = TestDTOProvider.createGetOneEnsemble();
        when(repositoryMock.getEnsembleRepository().findByIdAndAccountId(sessionManager, ensembleId, accountId))
            .thenReturn(Maybe.just(e1));
        when(repositoryMock.getEnsembleRepository().updateValidity(sessionManager, ensembleId, type.equals("valid")))
            .thenReturn(Completable.complete());
        try(MockedConstruction<EnsembleUtility> ignoreEnsemble = DatabaseUtilMockprovider
                .mockEnsembleUtilityFetchAndValidate(sessionManager, ensembleId, accountId, getOneEnsemble);
                MockedConstruction<SLOUtility> ignoreSLO = DatabaseUtilMockprovider
                    .mockSLOUtilityFindAndFilterResources(sessionManager, List.of(r1, r2));
                MockedStatic<EnsembleUtility> mockEnsembleUtil = mockStatic(EnsembleUtility.class)) {
            mockEnsembleUtil.when(() -> EnsembleUtility.getResourceEnsembleStatus(List.of(r1, r2),
                    getOneEnsemble.getResources())).thenReturn(List.of(r1es, r2es));

            validationUtility.validateAndUpdateEnsemble(sessionManager, ensembleId, accountId)
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.size()).isEqualTo(2);
                        assertThat(result.get(0).getIsValid()).isEqualTo(r1es.getIsValid());
                        assertThat(result.get(1).getIsValid()).isEqualTo(r2es.getIsValid());
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }
}
