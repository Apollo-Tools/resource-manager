package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.resourceprovider.ResourceProviderChecker;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
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
    private CredentialsChecker credentialsChecker;

    @Mock
    private AccountCredentialsChecker accountCredentialsChecker;

    @Mock
    private ResourceProviderChecker resourceProviderChecker;

    @Mock
    RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        credentialsHandler = new CredentialsHandler(credentialsChecker, accountCredentialsChecker,
            resourceProviderChecker);
    }


    @Test
    void getAll(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(1L);
        Credentials entity1 = TestAccountProvider.createCredentials(1L, new ResourceProvider());
        Credentials entity2 = TestAccountProvider.createCredentials(2L, new ResourceProvider());
        List<JsonObject> entities = new ArrayList<>();
        entities.add(JsonObject.mapFrom(entity1));
        entities.add(JsonObject.mapFrom(entity2));
        JsonArray resultJson = new JsonArray(entities);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(credentialsChecker.checkFindAll(account.getAccountId())).thenReturn(Single.just(resultJson));

        credentialsHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("credentials_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("credentials_id")).isEqualTo(2L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getAllEmpty(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(1L);
        List<JsonObject> entities = new ArrayList<>();
        JsonArray resultJson = new JsonArray(entities);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(credentialsChecker.checkFindAll(account.getAccountId())).thenReturn(Single.just(resultJson));

        credentialsHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void postOne(VertxTestContext testContext) {
        long accountId = 1L, providerId = 2L, credentialsId = 3L, accountCredentialsId = 4L;
        Account account = TestAccountProvider.createAccount(accountId);
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(providerId);
        Credentials credentials = TestAccountProvider.createCredentials(credentialsId, resourceProvider);
        AccountCredentials accountCredentials = TestAccountProvider
            .createAccountCredentials(accountCredentialsId, account, credentials);
        JsonObject requestBody = JsonObject.mapFrom(credentials);
        JsonObject accountCredentialsJson = new JsonObject("{" +
            "\"account\":{\"account_id\": " + accountId + "}," +
            "\"credentials\":{" +
            "   \"credentials_id\": " + credentialsId + "," +
            "   \"access_key\":\"accesskey\"," +
            "   \"secret_access_key\":\"secretaccesskey\"," +
            "   \"session_token\":\"sessiontoken\"," +
            "   \"resource_provider\":{\"provider_id\":" + providerId + ",\"provider\":\"aws\",\"created_at\":null},\"created_at\":null}}");

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(accountCredentialsChecker.checkForDuplicateEntity(requestBody, accountId))
            .thenReturn(Completable.complete());
        when(resourceProviderChecker.checkExistsOne(providerId)).thenReturn(Completable.complete());
        when(credentialsChecker.submitCreate(requestBody)).thenReturn(Single.just(requestBody));
        when(accountCredentialsChecker.submitCreate(accountCredentialsJson))
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
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void postOneProviderNotFound(VertxTestContext testContext) {
        long accountId = 1L, providerId = 2L, credentialsId = 3L;
        Account account = TestAccountProvider.createAccount(accountId);
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(providerId);
        Credentials credentials = TestAccountProvider.createCredentials(credentialsId, resourceProvider);
        JsonObject requestBody = JsonObject.mapFrom(credentials);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(accountCredentialsChecker.checkForDuplicateEntity(requestBody, accountId))
            .thenReturn(Completable.complete());
        when(resourceProviderChecker.checkExistsOne(providerId)).thenReturn(Completable.error(NotFoundException::new));

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
        Account account = TestAccountProvider.createAccount(accountId);
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(providerId);
        Credentials credentials = TestAccountProvider.createCredentials(credentialsId, resourceProvider);
        JsonObject requestBody = JsonObject.mapFrom(credentials);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(accountCredentialsChecker.checkForDuplicateEntity(requestBody, accountId))
            .thenReturn(Completable.error(AlreadyExistsException::new));
        when(resourceProviderChecker.checkExistsOne(providerId)).thenReturn(Completable.complete());

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
        Account account = TestAccountProvider.createAccount(accountId);
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(providerId);
        Credentials credentials = TestAccountProvider.createCredentials(credentialsId, resourceProvider);
        AccountCredentials accountCredentials = TestAccountProvider
            .createAccountCredentials(accountCredentialsId, account, credentials);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(credentialsId));
        when(credentialsChecker.checkFindOne(credentialsId)).thenReturn(Single.just(JsonObject.mapFrom(credentials)));
        when(accountCredentialsChecker.checkFindOneByCredentialsAndAccount(credentialsId, accountId))
            .thenReturn(Single.just(JsonObject.mapFrom(accountCredentials)));
        when(accountCredentialsChecker.submitDelete(accountCredentialsId)).thenReturn(Completable.complete());
        when(credentialsChecker.submitDelete(credentialsId)).thenReturn(Completable.complete());

        credentialsHandler.deleteOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void deleteOneAccountCredentialsNotExist(VertxTestContext testContext) {
        long accountId = 1L, providerId = 2L, credentialsId = 3L;
        Account account = TestAccountProvider.createAccount(accountId);
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(providerId);
        Credentials credentials = TestAccountProvider.createCredentials(credentialsId, resourceProvider);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(credentialsId));
        when(credentialsChecker.checkFindOne(credentialsId)).thenReturn(Single.just(JsonObject.mapFrom(credentials)));
        when(accountCredentialsChecker.checkFindOneByCredentialsAndAccount(credentialsId, accountId))
            .thenReturn(Single.error(NotFoundException::new));

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
        Account account = TestAccountProvider.createAccount(accountId);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(credentialsId));
        when(credentialsChecker.checkFindOne(credentialsId)).thenReturn(Single.error(NotFoundException::new));

        credentialsHandler.deleteOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
