package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.ensemble.ResourceEnsembleStatus;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.testutil.mockprovider.EnsembleRepositoryProviderMock;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link EnsembleUtility} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class EnsembleUtilityTest {

    private EnsembleUtility ensembleUtility;

    private final EnsembleRepositoryProviderMock repositoryMock = new EnsembleRepositoryProviderMock();

    @Mock
    private SessionManager sessionManager;

    private long ensembleId, accountId;
    private Resource r1,r2;
    private Ensemble e1;
    private EnsembleSLO eSLOStr, eSLONum, eSLOBool;
    private ServiceLevelObjective sloStr, sloNum, sloBool;

    @BeforeEach
    void initTest() {
        repositoryMock.mock();
        ensembleUtility = new EnsembleUtility(repositoryMock.getRepositoryProvider());
        ensembleId = 1L;
        accountId = 2L;
        r1 = TestResourceProvider.createClusterWithoutNodes(1L, "mainresource");
        r2 = TestResourceProvider.createSubResource(2L, "subresource", r1.getMain());
        e1 = TestEnsembleProvider.createEnsemble(ensembleId, accountId, "ensemble8");
        eSLOStr = TestEnsembleProvider.createEnsembleSLO(3L, "os", ensembleId, "linux");
        eSLONum = TestEnsembleProvider.createEnsembleSLOGT(4L, "availability", ensembleId, 0.95);
        eSLOBool = TestEnsembleProvider.createEnsembleSLO(5L, "online", ensembleId, true);
        sloStr = TestDTOProvider.createServiceLevelObjective("os", ExpressionType.EQ, "linux");
        sloNum = TestDTOProvider.createServiceLevelObjective("availability", ExpressionType.GT, 0.95);
        sloBool = TestDTOProvider.createServiceLevelObjective("online", ExpressionType.EQ, true);
    }

    @Test
    void fetchAndPopulateEnsemble(VertxTestContext testContext) {
        when(repositoryMock.getEnsembleRepository().findByIdAndAccountId(sessionManager, ensembleId, accountId))
            .thenReturn(Maybe.just(e1));
        when(repositoryMock.getResourceRepository().findAllByEnsembleId(sessionManager, ensembleId))
            .thenReturn(Single.just(List.of(r1, r2)));
        when(repositoryMock.getEnsembleSLORepository().findAllByEnsembleId(sessionManager, ensembleId))
            .thenReturn(Single.just(List.of(eSLOStr, eSLONum, eSLOBool)));

        ensembleUtility.fetchAndPopulateEnsemble(sessionManager, ensembleId, accountId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getEnsembleId()).isEqualTo(1L);
                    assertThat(result.getName()).isEqualTo("ensemble8");
                    assertThat(result.getRegions()).isEqualTo(List.of(1L, 2L));
                    assertThat(result.getProviders()).isEqualTo(List.of(3L, 4L));
                    assertThat(result.getResourceTypes()).isEqualTo(List.of(3L, 4L));
                    assertThat(result.getEnvironments()).isEqualTo(List.of(5L));
                    assertThat(result.getPlatforms()).isEqualTo(List.of(1L, 5L));
                    assertThat(result.getResources()).isEqualTo(List.of(r1, new SubResourceDTO((SubResource) r2)));
                    assertThat(result.getServiceLevelObjectives().size()).isEqualTo(3);
                    assertThat(result.getServiceLevelObjectives().get(0).getName()).isEqualTo("os");
                    assertThat(result.getServiceLevelObjectives().get(0).getValue().get(0).getValueString())
                        .isEqualTo("linux");
                    assertThat(result.getServiceLevelObjectives().get(0).getExpression().getSymbol())
                        .isEqualTo(ExpressionType.EQ.getSymbol());
                    assertThat(result.getServiceLevelObjectives().get(1).getName()).isEqualTo("availability");
                    assertThat(result.getServiceLevelObjectives().get(1).getValue().get(0).getValueNumber())
                        .isEqualTo(0.95);
                    assertThat(result.getServiceLevelObjectives().get(1).getExpression().getSymbol())
                        .isEqualTo(ExpressionType.GT.getSymbol());
                    assertThat(result.getServiceLevelObjectives().get(2).getName()).isEqualTo("online");
                    assertThat(result.getServiceLevelObjectives().get(2).getValue().get(0).getValueBool())
                        .isEqualTo(true);
                    assertThat(result.getServiceLevelObjectives().get(2).getExpression().getSymbol())
                        .isEqualTo(ExpressionType.EQ.getSymbol());
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void fetchAndPopulateEnsembleNotFound(VertxTestContext testContext) {
        when(repositoryMock.getEnsembleRepository().findByIdAndAccountId(sessionManager, ensembleId, accountId))
            .thenReturn(Maybe.empty());
        when(repositoryMock.getResourceRepository().findAllByEnsembleId(sessionManager, ensembleId))
            .thenReturn(Single.just(List.of(r1, r2)));
        when(repositoryMock.getEnsembleSLORepository().findAllByEnsembleId(sessionManager, ensembleId))
            .thenReturn(Single.just(List.of(eSLOStr, eSLONum, eSLOBool)));

        ensembleUtility.fetchAndPopulateEnsemble(sessionManager, ensembleId, accountId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"str", "num", "bool"})
    void createEnsembleSLO(String type) {
        ServiceLevelObjective slo;
        switch (type) {
            case "str":
                slo = sloStr;
                break;
            case "num":
                slo = sloNum;
                break;
            case "bool":
            default:
                slo = sloBool;
                break;
        }

        EnsembleSLO result = EnsembleUtility.createEnsembleSLO(slo, e1);
        assertThat(result.getEnsemble()).isEqualTo(e1);
        assertThat(result.getName()).isEqualTo(slo.getName());
        assertThat(result.getExpression()).isEqualTo(slo.getExpression());
        switch (type) {
            case "str":
                assertThat(result.getValueStrings()).isEqualTo(List.of("linux"));
                break;
            case "num":
                assertThat(result.getValueNumbers()).isEqualTo(List.of(0.95));
                break;
            case "bool":
                assertThat(result.getValueBools()).isEqualTo(List.of(true));
                break;
        }
    }

    @Test
    void getResourceEnsembleStatus() {
        Resource invalid = TestResourceProvider.createResource(5L);
        List<ResourceEnsembleStatus> result = ensembleUtility.getResourceEnsembleStatus(List.of(r1, r2), List.of(r1,
            invalid));
        assertThat(result.get(0).getIsValid()).isEqualTo(true);
        assertThat(result.get(1).getIsValid()).isEqualTo(false);
    }
}
