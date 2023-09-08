package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.AccountCredentials;
import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.account.AccountCredentialsRepository;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import at.uibk.dps.rm.service.database.util.SessionManager;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link CredentialsServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class CredentialsServiceImplTest {
    private CredentialsService credentialsService;

    @Mock
    private CredentialsRepository credentialsRepository;

    @Mock
    private AccountCredentialsRepository accountCredentialsRepository;

    @Mock
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;
    
    private SessionManager sessionManager;

    private Long accountId, providerId;
    private ResourceProvider rp;
    private Credentials credentials1;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        credentialsService = new CredentialsServiceImpl(credentialsRepository, accountCredentialsRepository,
            sessionFactory);
        accountId = 1L;
        providerId = 2L;
        rp = TestResourceProviderProvider.createResourceProvider(providerId);
        credentials1 = TestAccountProvider.createCredentials(accountId, rp);
        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void findAllByAccount(boolean includeSecrets, VertxTestContext testContext) {
        Credentials credentials2 = TestAccountProvider.createCredentials(2L, new ResourceProvider());
        List<Credentials> resultList = List.of(credentials1, credentials2);
        Single<List<Credentials>> single = Single.just(resultList);

        when(credentialsRepository.findAllByAccountId(sessionManager, accountId)).thenReturn(single);

        credentialsService.findAllByAccountIdAndIncludeExcludeSecrets(accountId, includeSecrets,
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("credentials_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("credentials_id")).isEqualTo(2L);
                if (includeSecrets) {
                    assertThat(result.getJsonObject(0).getString("access_key")).isEqualTo("accesskey");
                    assertThat(result.getJsonObject(1).getString("access_key")).isEqualTo("accesskey");
                    assertThat(result.getJsonObject(0).getString("secret_access_key"))
                        .isEqualTo("secretaccesskey");
                    assertThat(result.getJsonObject(1).getString("secret_access_key"))
                        .isEqualTo("secretaccesskey");
                    assertThat(result.getJsonObject(0).getString("session_token"))
                        .isEqualTo("sessiontoken");
                    assertThat(result.getJsonObject(1).getString("session_token"))
                        .isEqualTo("sessiontoken");
                } else {
                    assertThat(result.getJsonObject(0).getString("access_key")).isNull();
                    assertThat(result.getJsonObject(1).getString("access_key")).isNull();
                    assertThat(result.getJsonObject(0).getString("secret_access_key")).isNull();
                    assertThat(result.getJsonObject(1).getString("secret_access_key")).isNull();
                    assertThat(result.getJsonObject(0).getString("session_token")).isNull();
                    assertThat(result.getJsonObject(1).getString("session_token")).isNull();
                }
                testContext.completeNow();
            })));
    }

    @Test
    void saveToAccount(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(accountId);

        when(accountCredentialsRepository.findByAccountAndProvider(sessionManager, accountId, providerId))
            .thenReturn(Maybe.empty());
        when(session.find(ResourceProvider.class, providerId)).thenReturn(CompletionStages.completedFuture(rp));
        when(session.find(Account.class, accountId)).thenReturn(CompletionStages.completedFuture(account));
        when(session.persist(credentials1)).thenReturn(CompletionStages.voidFuture());
        when(session.persist(any(AccountCredentials.class))).thenReturn(CompletionStages.voidFuture());

        credentialsService.saveToAccount(accountId, JsonObject.mapFrom(credentials1),
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("credentials_id")).isEqualTo(1);
                assertThat(result.getString("access_key")).isEqualTo("accesskey");
                assertThat(result.getString("secret_access_key")).isEqualTo("secretaccesskey");
                assertThat(result.getString("session_token")).isEqualTo("sessiontoken");
                assertThat(result.containsKey("resource_provider")).isEqualTo(true);
                testContext.completeNow();
            })));
    }

    @Test
    void saveToAccountAccountNotFound(VertxTestContext testContext) {
        when(accountCredentialsRepository.findByAccountAndProvider(sessionManager, accountId, providerId))
            .thenReturn(Maybe.empty());
        when(session.find(ResourceProvider.class, providerId)).thenReturn(CompletionStages.completedFuture(rp));
        when(session.find(Account.class, accountId)).thenReturn(CompletionStages.nullFuture());

        credentialsService.saveToAccount(accountId, JsonObject.mapFrom(credentials1),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                assertThat(throwable.getMessage()).isEqualTo("unauthorized");
                testContext.completeNow();
            })));
    }

    @Test
    void saveToAccountProviderNotFound(VertxTestContext testContext) {
        when(accountCredentialsRepository.findByAccountAndProvider(sessionManager, accountId, providerId))
            .thenReturn(Maybe.empty());
        when(session.find(ResourceProvider.class, providerId)).thenReturn(CompletionStages.nullFuture());

        credentialsService.saveToAccount(accountId, JsonObject.mapFrom(credentials1),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                assertThat(throwable.getMessage()).isEqualTo("ResourceProvider not found");
                testContext.completeNow();
            })));
    }

    @Test
    void saveToAccountAlreadyExists(VertxTestContext testContext) {
        when(accountCredentialsRepository.findByAccountAndProvider(sessionManager, accountId, providerId))
            .thenReturn(Maybe.just(new AccountCredentials()));
        when(session.find(ResourceProvider.class, providerId)).thenReturn(CompletionStages.nullFuture());

        credentialsService.saveToAccount(accountId, JsonObject.mapFrom(credentials1),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                assertThat(throwable.getMessage()).isEqualTo("Credentials already exists");
                testContext.completeNow();
            })));
    }
}
