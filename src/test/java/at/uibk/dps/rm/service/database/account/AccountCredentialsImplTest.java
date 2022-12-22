package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.repository.AccountCredentialsRepository;
import at.uibk.dps.rm.testutil.TestObjectProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class AccountCredentialsImplTest {

    private AccountCredentialsService accountCredentialsService;

    @Mock
    AccountCredentialsRepository accountCredentialsRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        accountCredentialsService = new AccountCredentialsServiceImpl(accountCredentialsRepository);
    }

    @Test
    void findEntityByCredentialsAndAccountExists(VertxTestContext testContext) {
        long credentialsId = 1L;
        long accountId = 2L;
        Account account = TestObjectProvider.createAccount(accountId);
        Credentials credentials = TestObjectProvider.createCredentials(credentialsId, new ResourceProvider());
        AccountCredentials entity = TestObjectProvider
            .createAccountCredentials(1L, account, credentials);
        CompletionStage<AccountCredentials> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(accountCredentialsRepository)
            .findByCredentialsAndAccount(credentialsId, accountId);

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
            .findByCredentialsAndAccount(credentialsId, accountId);

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
        Account account = TestObjectProvider.createAccount(accountId);
        ResourceProvider resourceProvider = TestObjectProvider.createResourceProvider(providerId);
        Credentials credentials = TestObjectProvider.createCredentials(1L, resourceProvider);
        AccountCredentials entity = TestObjectProvider
            .createAccountCredentials(1L, account, credentials);
        CompletionStage<AccountCredentials> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(accountCredentialsRepository).findByAccountAndProvider(accountId, providerId);

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
        doReturn(completionStage).when(accountCredentialsRepository).findByAccountAndProvider(accountId, providerId);

        accountCredentialsService.existsOneByAccountAndProvider(accountId, providerId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                testContext.completeNow();
            })));
    }
}