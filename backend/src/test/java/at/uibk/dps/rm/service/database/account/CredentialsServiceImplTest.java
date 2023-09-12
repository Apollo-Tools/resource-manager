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
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
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
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    private Long accountId, providerId;
    private ResourceProvider rp;
    private Credentials credentials1;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        credentialsService = new CredentialsServiceImpl(credentialsRepository, accountCredentialsRepository,
            smProvider);
        accountId = 1L;
        providerId = 2L;
        rp = TestResourceProviderProvider.createResourceProvider(providerId);
        credentials1 = TestAccountProvider.createCredentials(accountId, rp);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void findAllByAccount(boolean includeSecrets, VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
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
        AccountCredentials accountCredentials = TestAccountProvider.createAccountCredentials(1L,
            account, credentials1);

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(accountCredentialsRepository.findByAccountAndProvider(sessionManager, accountId, providerId))
            .thenReturn(Maybe.empty());
        when(sessionManager.find(ResourceProvider.class, providerId)).thenReturn(Maybe.just(rp));
        when(sessionManager.find(Account.class, accountId)).thenReturn(Maybe.just(account));
        when(sessionManager.persist(credentials1)).thenReturn(Single.just(credentials1));
        when(sessionManager.persist(argThat((Object obj) -> {
            if (obj instanceof AccountCredentials) {
                AccountCredentials ac = (AccountCredentials) obj;
                return ac.getAccount().equals(account) &&
                    ac.getCredentials().equals(credentials1);
            }
            return false;
        })))
            .thenReturn(Single.just(accountCredentials));

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
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(accountCredentialsRepository.findByAccountAndProvider(sessionManager, accountId, providerId))
            .thenReturn(Maybe.empty());
        when(sessionManager.find(ResourceProvider.class, providerId)).thenReturn(Maybe.just(rp));
        when(sessionManager.find(Account.class, accountId)).thenReturn(Maybe.empty());

        credentialsService.saveToAccount(accountId, JsonObject.mapFrom(credentials1),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                assertThat(throwable.getMessage()).isEqualTo("unauthorized");
                testContext.completeNow();
            })));
    }

    @Test
    void saveToAccountProviderNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(accountCredentialsRepository.findByAccountAndProvider(sessionManager, accountId, providerId))
            .thenReturn(Maybe.empty());
        when(sessionManager.find(ResourceProvider.class, providerId)).thenReturn(Maybe.empty());

        credentialsService.saveToAccount(accountId, JsonObject.mapFrom(credentials1),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                assertThat(throwable.getMessage()).isEqualTo("ResourceProvider not found");
                testContext.completeNow();
            })));
    }

    @Test
    void saveToAccountAlreadyExists(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(accountCredentialsRepository.findByAccountAndProvider(sessionManager, accountId, providerId))
            .thenReturn(Maybe.just(new AccountCredentials()));
        when(sessionManager.find(ResourceProvider.class, providerId)).thenReturn(Maybe.empty());

        credentialsService.saveToAccount(accountId, JsonObject.mapFrom(credentials1),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                assertThat(throwable.getMessage()).isEqualTo("Credentials already exists");
                testContext.completeNow();
            })));
    }
}
