package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link EnsembleHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class EnsembleHandlerTest {

    private EnsembleHandler ensembleHandler;

    @Mock
    private EnsembleChecker ensembleChecker;

    @Mock
    private EnsembleSLOChecker ensembleSLOChecker;

    @Mock
    private ResourceEnsembleChecker resourceEnsembleChecker;

    @Mock
    private ResourceChecker resourceChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        ensembleHandler = new EnsembleHandler(ensembleChecker, ensembleSLOChecker, resourceEnsembleChecker,
            resourceChecker);
    }

    private static Stream<Arguments> providePostOneInput() {
        return Stream.of(
            Arguments.of("valid"),
            Arguments.of("resourceNotFound"),
            Arguments.of("alreadyExists")
        );
    }

    @ParameterizedTest
    @MethodSource("providePostOneInput")
    void postOneValid(String testCase, VertxTestContext testContext) {
        JsonObject body = new JsonObject("{\"name\": \"ensemble\", \"slos\": [" +
            "{\"name\": \"region\",\"expression\": \"==\",\"value\":[1, 3]}, " +
            "{\"name\": \"availability\",\"expression\": \">\",\"value\":[0.8]}, " +
            "{\"name\": \"os\",\"expression\": \"==\",\"value\":[\"ubuntu\"]}," +
            "{\"name\": \"online\",\"expression\": \"==\",\"value\":[true]}],\"resources\": [{\"resource_id\": 1}," +
            "{\"resource_id\": 2}]}");
        Account account = TestAccountProvider.createAccount(1L);
        Ensemble ensemble = TestEnsembleProvider.createEnsemble(1L, 1L);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, body);
        if (testCase.equals("alreadyExists")) {
            when(ensembleChecker.checkExistsOneByName("ensemble", 1L))
                .thenReturn(Completable.error(AlreadyExistsException::new));
        }
        if (testCase.equals("resourceNotFound")) {
            when(ensembleChecker.checkExistsOneByName("ensemble", 1L)).thenReturn(Completable.complete());
            when(resourceChecker.checkExistsOne(or(eq(1L), eq(2L))))
                .thenReturn(Completable.error(NotFoundException::new));
        }
        if (testCase.equals("valid")) {
            when(ensembleChecker.checkExistsOneByName("ensemble", 1L)).thenReturn(Completable.complete());
            when(resourceChecker.checkExistsOne(or(eq(1L), eq(2L)))).thenReturn(Completable.complete());
            when(ensembleChecker.submitCreate(any(JsonObject.class)))
                .thenReturn(Single.just(JsonObject.mapFrom(ensemble)));
            when(resourceEnsembleChecker.submitCreateAll(any(JsonArray.class)))
                .thenReturn(Completable.complete());
            when(ensembleSLOChecker.submitCreateAll(any(JsonArray.class)))
                .thenReturn(Completable.complete());
        }

        ensembleHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                if (testCase.equals("valid")) {
                    assertThat(result.getLong("ensemble_id")).isEqualTo(1L);
                    assertThat(result.containsKey("slos")).isEqualTo(false);
                    assertThat(result.containsKey("regions")).isEqualTo(false);
                    assertThat(result.containsKey("providers")).isEqualTo(false);
                    assertThat(result.containsKey("resource_types")).isEqualTo(false);
                    assertThat(result.containsKey("created_by")).isEqualTo(false);
                } else {
                    fail("method did not throw exception");
                }
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> {
                    if (testCase.equals("valid")) {
                        fail("method has thrown exception");
                    } else if (testCase.equals("resourceNotFound")) {
                        assertThat(throwable).isInstanceOf(NotFoundException.class);
                    } else {
                        assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    }
                    testContext.completeNow();
                })
            );
    }

    @Test
    void getOne(VertxTestContext testContext) {
        long ensembleId = 1L;
        Account account = TestAccountProvider.createAccount(1L);
        Ensemble ensemble = TestEnsembleProvider.createEnsemble(1L, 1L);
        JsonObject r1 = JsonObject.mapFrom(TestResourceProvider.createResource(1L));
        JsonObject r2 = JsonObject.mapFrom(TestResourceProvider.createResource(2L));
        JsonObject slo1 = JsonObject.mapFrom(TestEnsembleProvider
            .createEnsembleSLO(1L, "os", ensembleId, "ubuntu"));
        JsonObject slo2 = JsonObject.mapFrom(TestEnsembleProvider
            .createEnsembleSLO(2L, "online", ensembleId, true));
        JsonObject slo3 = JsonObject.mapFrom(TestEnsembleProvider
            .createEnsembleSLO(3L, "availability", ensembleId));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(ensembleId));
        when(ensembleChecker.checkFindOne(ensembleId,account.getAccountId()))
            .thenReturn(Single.just(JsonObject.mapFrom(ensemble)));
        when(resourceChecker.checkFindAllByEnsemble(ensembleId))
            .thenReturn(Single.just(new JsonArray(List.of(r1, r2))));
        when(ensembleSLOChecker.checkFindAllByEnsemble(ensembleId))
            .thenReturn(Single.just(new JsonArray(List.of(slo1, slo2, slo3))));

        ensembleHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("ensemble_id")).isEqualTo(1L);
                    assertThat(result.getJsonArray("resources").size()).isEqualTo(2);
                    assertThat(result.getJsonArray("slos").size()).isEqualTo(8);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getOneNotFound(VertxTestContext testContext) {
        long ensembleId = 1L;
        Account account = TestAccountProvider.createAccount(1L);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(ensembleId));
        when(ensembleChecker.checkFindOne(ensembleId,account.getAccountId()))
            .thenReturn(Single.error(NotFoundException::new));

        ensembleHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
            }));
    }

    @Test
    void getAll(VertxTestContext testContext) {
        long accountId = 1L;
        JsonObject e1 = JsonObject.mapFrom(TestEnsembleProvider.createEnsemble(1L, accountId));
        JsonObject e2 = JsonObject.mapFrom(TestEnsembleProvider.createEnsemble(2L, accountId));
        JsonObject e3 = JsonObject.mapFrom(TestEnsembleProvider.createEnsemble(3L, accountId));
        JsonArray ensemblesJson = new JsonArray(List.of(e1, e2, e3));
        Account account = TestAccountProvider.createAccount(accountId);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(ensembleChecker.checkFindAll(accountId)).thenReturn(Single.just(ensemblesJson));

        ensembleHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    for (int i = 0; i < 3; i++) {
                        assertThat(result.getJsonObject(i).getLong("ensemble_id")).isEqualTo(i+1);
                    }
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void deleteOne(VertxTestContext testContext) {
        long ensembleId = 1L, accountId = 2L;
        Account account = TestAccountProvider.createAccount(accountId);
        Ensemble ensemble = TestEnsembleProvider.createEnsemble(ensembleId, accountId);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(ensembleId));
        when(ensembleChecker.checkFindOne(ensembleId, accountId)).thenReturn(Single.just(JsonObject.mapFrom(ensemble)));
        when(ensembleChecker.submitDelete(ensembleId)).thenReturn(Completable.complete());

        ensembleHandler.deleteOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void deleteOneNotFound(VertxTestContext testContext) {
        long ensembleId = 1L, accountId = 2L;
        Account account = TestAccountProvider.createAccount(accountId);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(ensembleId));
        when(ensembleChecker.checkFindOne(ensembleId, accountId)).thenReturn(Single.error(NotFoundException::new));

        ensembleHandler.deleteOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
            throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            }));
    }
}
