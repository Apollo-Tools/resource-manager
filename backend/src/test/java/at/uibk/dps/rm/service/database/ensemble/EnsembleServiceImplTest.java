package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.ensemble.ResourceEnsembleStatus;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.database.util.*;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.mockprovider.DatabaseUtilMockprovider;
import at.uibk.dps.rm.testutil.mockprovider.EnsembleRepositoryProviderMock;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

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
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    private long accountId;
    private Account account;
    private Ensemble e1, e2;
    private Resource r1, r2;
    private ResourceEnsembleStatus res1Valid, res1Invalid, res2Invalid;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        repositoryMock.mock();
        ensembleService = new EnsembleServiceImpl(repositoryMock.getRepositoryProvider(), smProvider);
        accountId = 1L;
        account = TestAccountProvider.createAccount(accountId);
        e1 = TestEnsembleProvider.createEnsemble(1L, accountId);
        e2 = TestEnsembleProvider.createEnsemble(2L, accountId);
        r1 = TestResourceProvider.createResource(3L);
        r2 = TestResourceProvider.createResource(4L);
        res1Valid = new ResourceEnsembleStatus(r1.getResourceId(), true);
        res1Invalid = new ResourceEnsembleStatus(r1.getResourceId(), false);
        res2Invalid = new ResourceEnsembleStatus(r2.getResourceId(), false);
    }

    @Test
    void findAll(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getEnsembleRepository().findAllAndFetch(sessionManager))
            .thenReturn(Single.just(List.of(e1, e2)));

        ensembleService.findAll(testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.getJsonObject(0).getLong("ensemble_id")).isEqualTo(1L);
            assertThat(result.getJsonObject(1).getLong("ensemble_id")).isEqualTo(2L);
            assertThat(result.getJsonObject(0).getString("name")).isEqualTo("ensemble" + 1);
            assertThat(result.getJsonObject(1).getString("name")).isEqualTo("ensemble" + 2);
            testContext.completeNow();
        })));
    }

    @Test
    void findAllByAccountId(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getEnsembleRepository().findAllByAccountId(sessionManager, accountId))
            .thenReturn(Single.just(List.of(e1, e2)));

        ensembleService.findAllByAccountId(accountId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                for(int i=0; i<2; i++) {
                    assertThat(result.getJsonObject(i).getLong("ensemble_id")).isEqualTo(i+1);
                    assertThat(result.getJsonObject(i).getString("name")).isEqualTo("ensemble" + (i+1));
                    assertThat(result.getJsonObject(i).getValue("resource_types")).isNull();
                    assertThat(result.getJsonObject(i).getValue("regions")).isNull();
                    assertThat(result.getJsonObject(i).getValue("providers")).isNull();
                    assertThat(result.getJsonObject(i).getValue("environments")).isNull();
                    assertThat(result.getJsonObject(i).getValue("platforms")).isNull();
                }
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByIdAndAccountId(VertxTestContext testContext) {
        GetOneEnsemble getOneEnsemble = TestDTOProvider.createGetOneEnsemble();

        SessionMockHelper.mockSingle(smProvider, sessionManager);

        try (MockedConstruction<EnsembleUtility> ignored = DatabaseUtilMockprovider.mockEnsembleUtilityFetch(
            sessionManager, e1.getEnsembleId(), accountId, getOneEnsemble)) {
            ensembleService.findOneByIdAndAccountId(e1.getEnsembleId(), accountId,
                testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.getLong("ensemble_id")).isEqualTo(1L);
                    assertThat(result.getString("name")).isEqualTo("ensemble");
                    testContext.completeNow();
                }))
            );
        }
    }

    @Test
    void findOne(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(sessionManager.find(Ensemble.class, e1.getEnsembleId())).thenReturn(Maybe.just(e1));

        ensembleService.findOne(e1.getEnsembleId(), testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(result.getLong("ensemble_id")).isEqualTo(1L);
            testContext.completeNow();
        })));
    }

    @Test
    void findOneNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(sessionManager.find(Ensemble.class, e1.getEnsembleId())).thenReturn(Maybe.empty());

        ensembleService.findOne(e1.getEnsembleId(), testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(NotFoundException.class);
            testContext.completeNow();
        })));
    }

    private boolean compareEnsembleName(Object ensemble, String name) {
        if (!(ensemble instanceof Ensemble)) {
            return false;
        }
        Ensemble ens = (Ensemble) ensemble;
        return ens.getName().equals(name);
    }

    private boolean compareResourceEnsemble(Object resourceEnsemble, Ensemble ensemble, Resource resource) {
        if (!(resourceEnsemble instanceof ResourceEnsemble)) {
            return false;
        }
        ResourceEnsemble re = (ResourceEnsemble) resourceEnsemble;
        return re.getEnsemble().equals(ensemble) && re.getResource().equals(resource);
    }

    private boolean compareEnsembleSLO(Object ensembleSLO, String name) {
        if (!(ensembleSLO instanceof EnsembleSLO)) {
            return false;
        }
        EnsembleSLO eSLO = (EnsembleSLO) ensembleSLO;
        return eSLO.getName().equals(name);
    }

    @Test
    void saveToAccount(VertxTestContext testContext) {
        CreateEnsembleRequest request = TestDTOProvider.createCreateEnsembleRequest(r1.getResourceId());

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getEnsembleRepository()
            .findByNameAndAccountId(sessionManager, request.getName(), accountId)).thenReturn(Maybe.empty());
        when(sessionManager.find(Account.class, accountId)).thenReturn(Maybe.just(account));
        doReturn(Single.just(e1)).when(sessionManager)
            .persist(argThat((Object obj) -> compareEnsembleName(obj, request.getName())));
        doReturn(Maybe.just(r1)).when(sessionManager).find(Resource.class, r1.getResourceId());
        doReturn(Single.just(new ResourceEnsemble())).when(sessionManager)
            .persist(argThat((Object obj) -> compareResourceEnsemble(obj, e1, r1)));
        doReturn(Single.just(new EnsembleSLO())).when(sessionManager)
            .persist(argThat((Object obj) -> compareEnsembleSLO(obj, "availability")));

        ensembleService.saveToAccount(accountId, JsonObject.mapFrom(request),
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("ensemble_id")).isEqualTo(1L);
                assertThat(result.getValue("environments")).isNull();
                assertThat(result.getValue("resource_types")).isNull();
                assertThat(result.getValue("platforms")).isNull();
                assertThat(result.getValue("regions")).isNull();
                assertThat(result.getValue("providers")).isNull();
                assertThat(result.getValue("created_by")).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void saveToAccountResourceNotFound(VertxTestContext testContext) {
        CreateEnsembleRequest request = TestDTOProvider.createCreateEnsembleRequest(r1.getResourceId());

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getEnsembleRepository()
            .findByNameAndAccountId(sessionManager, request.getName(), accountId)).thenReturn(Maybe.empty());
        when(sessionManager.find(Account.class, accountId)).thenReturn(Maybe.just(account));
        doReturn(Single.just(e1)).when(sessionManager)
            .persist(argThat((Object obj) -> compareEnsembleName(obj, request.getName())));
        doReturn(Maybe.empty()).when(sessionManager).find(Resource.class, r1.getResourceId());

        ensembleService.saveToAccount(accountId, JsonObject.mapFrom(request),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void saveToAccountAccountNotFound(VertxTestContext testContext) {
        CreateEnsembleRequest request = TestDTOProvider.createCreateEnsembleRequest(r1.getResourceId());

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getEnsembleRepository()
            .findByNameAndAccountId(sessionManager, request.getName(), accountId)).thenReturn(Maybe.empty());
        when(sessionManager.find(Account.class, accountId)).thenReturn(Maybe.empty());

        ensembleService.saveToAccount(accountId, JsonObject.mapFrom(request),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void saveToAccountAlreadyExists(VertxTestContext testContext) {
        CreateEnsembleRequest request = TestDTOProvider.createCreateEnsembleRequest(r1.getResourceId());

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getEnsembleRepository()
            .findByNameAndAccountId(sessionManager, request.getName(), accountId)).thenReturn(Maybe.just(e1));
        when(sessionManager.find(Account.class, accountId)).thenReturn(Maybe.empty());

        ensembleService.saveToAccount(accountId, JsonObject.mapFrom(request),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void validateExistingEnsemble(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        try (MockedConstruction<EnsembleValidationUtility> ignored = DatabaseUtilMockprovider.mockEnsembleValidationUtility(
            sessionManager, e1.getEnsembleId(), accountId, List.of(res1Valid, res2Invalid))) {
            ensembleService.validateExistingEnsemble(accountId, e1.getEnsembleId(),
                testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getBoolean("is_valid")).isEqualTo(true);
                    assertThat(result.getJsonObject(1).getBoolean("is_valid")).isEqualTo(false);
                    testContext.completeNow();
                })));
        }
    }

    @Test
    void validateAllExistingEnsemble(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getEnsembleRepository().findAll(sessionManager)).thenReturn(Single.just(List.of(e1, e2)));
        Map<Ensemble, List<ResourceEnsembleStatus>> ensembleMap = Map.of(e1, List.of(res1Valid), e2,
            List.of(res1Invalid, res2Invalid));
        try (MockedConstruction<EnsembleValidationUtility> ignored = DatabaseUtilMockprovider.mockEnsembleValidationUtilityList(
            sessionManager, accountId, ensembleMap)) {
            ensembleService.validateAllExistingEnsembles(testContext.succeeding(result ->
                testContext.verify(testContext::completeNow)));
        }
    }
}
