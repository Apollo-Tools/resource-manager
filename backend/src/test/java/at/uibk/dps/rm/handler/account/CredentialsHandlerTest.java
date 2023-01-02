package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountCredentialsService;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.ResourceProviderService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.TestObjectProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class CredentialsHandlerTest {

    private CredentialsHandler credentialsHandler;

    @Mock
    private CredentialsService credentialsService;

    @Mock
    private AccountCredentialsService accountCredentialsService;

    @Mock
    private ResourceProviderService resourceProviderService;

    @Mock
    RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        credentialsHandler = new CredentialsHandler(credentialsService, accountCredentialsService,
            resourceProviderService);
    }


    @Test
    void getAll(VertxTestContext testContext) {
        Account account = TestObjectProvider.createAccount(1L);
        Credentials entity1 = TestObjectProvider.createCredentials(1L, new ResourceProvider());
        Credentials entity2 = TestObjectProvider.createCredentials(2L, new ResourceProvider());
        List<JsonObject> entities = new ArrayList<>();
        entities.add(JsonObject.mapFrom(entity1));
        entities.add(JsonObject.mapFrom(entity2));
        JsonArray resultJson = new JsonArray(entities);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(credentialsService.findAllByAccountId(account.getAccountId())).thenReturn(Single.just(resultJson));

        credentialsHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("credentials_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("credentials_id")).isEqualTo(2L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void getAllEmpty(VertxTestContext testContext) {
        Account account = TestObjectProvider.createAccount(1L);
        List<JsonObject> entities = new ArrayList<>();
        JsonArray resultJson = new JsonArray(entities);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(credentialsService.findAllByAccountId(account.getAccountId())).thenReturn(Single.just(resultJson));

        credentialsHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void postOne(VertxTestContext testContext) {
        long accountId = 1L, providerId = 2L, credentialsId = 3L, accountCredentialsId = 4L;
        Account account = TestObjectProvider.createAccount(accountId);
        ResourceProvider resourceProvider = TestObjectProvider.createResourceProvider(providerId);
        Credentials credentials = TestObjectProvider.createCredentials(credentialsId, resourceProvider);
        AccountCredentials accountCredentials = TestObjectProvider
            .createAccountCredentials(accountCredentialsId, account, credentials);
        JsonObject credentialsJson = JsonObject.mapFrom(credentials);
        JsonObject accountCredentialsJson = new JsonObject("{" +
            "\"account\":{\"account_id\": " + accountId + "}," +
            "\"credentials\":{" +
            "   \"credentials_id\": " + credentialsId + "," +
            "   \"access_key\":\"accesskey\"," +
            "   \"secret_access_key\":\"secretaccesskey\"," +
            "   \"session_token\":\"sessiontoken\"," +
            "   \"resource_provider\":{\"provider_id\":" + providerId + ",\"provider\":\"aws\",\"created_at\":null},\"created_at\":null}}");

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, credentialsJson);
        when(accountCredentialsService.existsOneByAccountAndProvider(accountId, providerId))
            .thenReturn(Single.just(false));
        when(resourceProviderService.existsOneById(providerId)).thenReturn(Single.just(true));
        when(credentialsService.save(credentialsJson)).thenReturn(Single.just(credentialsJson));
        when(accountCredentialsService.save(accountCredentialsJson))
            .thenReturn(Single.just(JsonObject.mapFrom(accountCredentials)));

        credentialsHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("account_credentials_id")).isEqualTo(accountCredentialsId);
                    assertThat(result.getJsonObject("account").getLong("account_id"))
                        .isEqualTo(accountId);
                    assertThat(result.getJsonObject("credentials").getLong("credentials_id"))
                        .isEqualTo(credentialsId);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void postOneProviderNotFound(VertxTestContext testContext) {
        long accountId = 1L, providerId = 2L, credentialsId = 3L;
        Account account = TestObjectProvider.createAccount(accountId);
        ResourceProvider resourceProvider = TestObjectProvider.createResourceProvider(providerId);
        Credentials credentials = TestObjectProvider.createCredentials(credentialsId, resourceProvider);
        JsonObject credentialsJson = JsonObject.mapFrom(credentials);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, credentialsJson);
        when(accountCredentialsService.existsOneByAccountAndProvider(accountId, providerId))
            .thenReturn(Single.just(false));
        when(resourceProviderService.existsOneById(providerId)).thenReturn(Single.just(false));

        credentialsHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postOneAlreadyExists(VertxTestContext testContext) {
        long accountId = 1L, providerId = 2L, credentialsId = 3L;
        Account account = TestObjectProvider.createAccount(accountId);
        ResourceProvider resourceProvider = TestObjectProvider.createResourceProvider(providerId);
        Credentials credentials = TestObjectProvider.createCredentials(credentialsId, resourceProvider);
        JsonObject credentialsJson = JsonObject.mapFrom(credentials);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, credentialsJson);
        when(accountCredentialsService.existsOneByAccountAndProvider(accountId, providerId))
            .thenReturn(Single.just(true));
        when(resourceProviderService.existsOneById(providerId)).thenReturn(Single.just(true));

        credentialsHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void deleteOneExists(VertxTestContext testContext) {
        long accountId = 1L, providerId = 2L, credentialsId = 3L, accountCredentialsId = 4L;
        Account account = TestObjectProvider.createAccount(accountId);
        ResourceProvider resourceProvider = TestObjectProvider.createResourceProvider(providerId);
        Credentials credentials = TestObjectProvider.createCredentials(credentialsId, resourceProvider);
        AccountCredentials accountCredentials = TestObjectProvider
            .createAccountCredentials(accountCredentialsId, account, credentials);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(credentialsId));
        when(credentialsService.findOne(credentialsId)).thenReturn(Single.just(JsonObject.mapFrom(credentials)));
        when(accountCredentialsService.findOneByCredentialsAndAccount(credentialsId, accountId))
            .thenReturn(Single.just(JsonObject.mapFrom(accountCredentials)));
        when(accountCredentialsService.delete(accountCredentialsId)).thenReturn(Completable.complete());
        when(credentialsService.delete(credentialsId)).thenReturn(Completable.complete());

        credentialsHandler.deleteOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
        testContext.completeNow();
    }

    @Test
    void deleteOneAccountCredentialsNotExist(VertxTestContext testContext) {
        long accountId = 1L, providerId = 2L, credentialsId = 3L;
        Account account = TestObjectProvider.createAccount(accountId);
        ResourceProvider resourceProvider = TestObjectProvider.createResourceProvider(providerId);
        Credentials credentials = TestObjectProvider.createCredentials(credentialsId, resourceProvider);
        Single<JsonObject> handler = new SingleHelper<JsonObject>().getEmptySingle();

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(credentialsId));
        when(credentialsService.findOne(credentialsId)).thenReturn(Single.just(JsonObject.mapFrom(credentials)));
        when(accountCredentialsService.findOneByCredentialsAndAccount(credentialsId, accountId))
            .thenReturn(handler);

        credentialsHandler.deleteOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void deleteOneCredentialsNotExist(VertxTestContext testContext) {
        long accountId = 1L, credentialsId = 3L;
        Account account = TestObjectProvider.createAccount(accountId);
        Single<JsonObject> handler = new SingleHelper<JsonObject>().getEmptySingle();

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(credentialsId));
        when(credentialsService.findOne(credentialsId)).thenReturn(handler);

        credentialsHandler.deleteOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
