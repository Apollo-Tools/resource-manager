package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.repository.account.AccountCredentialsRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage.SessionFactory;
import org.hibernate.reactive.stage.Stage.Session;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

/**
 * Implements tests for the {@link AccountCredentialsImplTest} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class AccountCredentialsImplTest {

    private AccountCredentialsService accountCredentialsService;

    @Mock
    private AccountCredentialsRepository accountCredentialsRepository;

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        accountCredentialsService = new AccountCredentialsServiceImpl(accountCredentialsRepository, sessionFactory);
    }

    @Test
    void findEntityByCredentialsAndAccountExists(VertxTestContext testContext) {
        long credentialsId = 1L;
        long accountId = 2L;
        Account account = TestAccountProvider.createAccount(accountId);
        Credentials credentials = TestAccountProvider.createCredentials(credentialsId, new ResourceProvider());
        AccountCredentials entity = TestAccountProvider
            .createAccountCredentials(1L, account, credentials);
        CompletionStage<AccountCredentials> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(accountCredentialsRepository)
            .findByCredentialsAndAccount(session, credentialsId, accountId);

        accountCredentialsService.findOneByCredentialsAndAccount(credentialsId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("account_credentials_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("credentials")
                    .getLong("credentials_id")).isEqualTo(credentialsId);
                assertThat(result.getJsonObject("credentials")
                    .getJsonObject("resource_provider")).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findEntityByCredentialsAndAccountNotExists(VertxTestContext testContext) {
        long credentialsId = 1L;
        long accountId = 2L;
        CompletionStage<AccountCredentials> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(accountCredentialsRepository)
            .findByCredentialsAndAccount(session, credentialsId, accountId);

        accountCredentialsService.findOneByCredentialsAndAccount(credentialsId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void checkEntityByAccountAndProviderExists(VertxTestContext testContext) {
        long accountId = 1L;
        long providerId = 2L;
        Account account = TestAccountProvider.createAccount(accountId);
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(providerId);
        Credentials credentials = TestAccountProvider.createCredentials(1L, resourceProvider);
        AccountCredentials entity = TestAccountProvider
            .createAccountCredentials(1L, account, credentials);
        CompletionStage<AccountCredentials> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(accountCredentialsRepository)
            .findByAccountAndProvider(session, accountId, providerId);

        accountCredentialsService.existsOneByAccountAndProvider(accountId, providerId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                testContext.completeNow();
            })));
    }
    @Test
    void checkEntityByAccountAndProviderNotExists(VertxTestContext testContext) {
        long accountId = 1L;
        long providerId = 2L;
        CompletionStage<AccountCredentials> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(accountCredentialsRepository)
            .findByAccountAndProvider(session, accountId, providerId);

        accountCredentialsService.existsOneByAccountAndProvider(accountId, providerId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                testContext.completeNow();
            })));
    }
}
