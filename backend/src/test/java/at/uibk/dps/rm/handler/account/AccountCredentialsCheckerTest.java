package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.AccountCredentials;
import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountCredentialsService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class AccountCredentialsCheckerTest {

    AccountCredentialsChecker accountCredentialsChecker;

    @Mock
    AccountCredentialsService accountCredentialsService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        accountCredentialsChecker = new AccountCredentialsChecker(accountCredentialsService);
    }

    @Test
    void checkForDuplicateEntityFalse(VertxTestContext testContext) {
        long accountId = 1L, providerId = 2L;
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(providerId);
        Credentials credentials = TestAccountProvider.createCredentials(1L, resourceProvider);

        when(accountCredentialsService.existsOneByAccountAndProvider(accountId, providerId))
            .thenReturn(Single.just(false));

        accountCredentialsChecker.checkForDuplicateEntity(JsonObject.mapFrom(credentials), accountId)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );

        verify(accountCredentialsService).existsOneByAccountAndProvider(accountId, providerId);
        testContext.completeNow();
    }

    @Test
    void checkForDuplicateEntityTrue(VertxTestContext testContext) {
        long accountId = 1L, providerId = 2L;
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(providerId);
        Credentials credentials = TestAccountProvider.createCredentials(1L, resourceProvider);

        when(accountCredentialsService.existsOneByAccountAndProvider(accountId, providerId))
            .thenReturn(Single.just(true));

        accountCredentialsChecker.checkForDuplicateEntity(JsonObject.mapFrom(credentials), accountId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkFindOneByCredentialsAndAccountExists(VertxTestContext testContext) {
        long accountId = 1L, credentialsId = 2L, accountCredentialsId = 3L;
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L);
        Credentials credentials = TestAccountProvider.createCredentials(credentialsId, resourceProvider);
        Account account = TestAccountProvider.createAccount(accountId);
        AccountCredentials accountCredentials = TestAccountProvider
            .createAccountCredentials(accountCredentialsId, account, credentials);

        when(accountCredentialsService.findOneByCredentialsAndAccount(credentialsId, accountId))
            .thenReturn(Single.just(JsonObject.mapFrom(accountCredentials)));

        accountCredentialsChecker.checkFindOneByCredentialsAndAccount(credentialsId, accountId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("account_credentials_id"))
                        .isEqualTo(accountCredentialsId);
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
    void checkFindOneByCredentialsAndAccountNotExists(VertxTestContext testContext) {
        long accountId = 1L, credentialsId = 2L;
        Single<JsonObject> handler = SingleHelper.getEmptySingle();

        when(accountCredentialsService.findOneByCredentialsAndAccount(credentialsId, accountId))
            .thenReturn(handler);

        accountCredentialsChecker.checkFindOneByCredentialsAndAccount(credentialsId, accountId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
