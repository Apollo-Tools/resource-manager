package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
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
public class AccountServiceImplTest {

    private AccountService accountService;

    @Mock
    AccountRepository accountRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        accountService = new AccountServiceImpl(accountRepository);
    }

    @Test
    void findEntityByUsernameExists(VertxTestContext testContext) {
        String username = "user1";
        Account entity = TestAccountProvider.createAccount(1L, "user1", "password");
        CompletionStage<Account> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(accountRepository).findByUsername(username);

        accountService.findOneByUsername(username)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("account_id")).isEqualTo(1L);
                assertThat(result.getString("username")).isEqualTo(username);
                testContext.completeNow();
            })));
    }

    @Test
    void findEntityByUsernameNotExists(VertxTestContext testContext) {
        String username = "user1";
        CompletionStage<Account> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(accountRepository).findByUsername(username);

        accountService.findOneByUsername(username)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void checkEntityByUsernameExists(VertxTestContext testContext) {
        String username = "user1";
        Account entity = TestAccountProvider.createAccount(1L, "user1", "password");
        CompletionStage<Account> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(accountRepository).findByUsername(username, true);

        accountService.existsOneByUsername(username, true)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                testContext.completeNow();
            })));
    }

    @Test
    void checkEntityByUsernameNotExists(VertxTestContext testContext) {
        String username = "user1";
        CompletionStage<Account> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(accountRepository).findByUsername(username, true);

        accountService.existsOneByUsername(username, true)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                testContext.completeNow();
            })));
    }
}
