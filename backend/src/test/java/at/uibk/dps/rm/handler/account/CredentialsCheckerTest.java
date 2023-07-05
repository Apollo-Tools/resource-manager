package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link CredentialsChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class CredentialsCheckerTest {

    CredentialsChecker credentialsChecker;

    @Mock
    CredentialsService credentialsService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        credentialsChecker = new CredentialsChecker(credentialsService);
    }

    @Test
    void checkExistsOneByProviderIdTrue(VertxTestContext testContext) {
        long accountId = 1L, providerId = 2L;

        when(credentialsService.existsOneByAccountIdAndProviderId(accountId, providerId))
            .thenReturn(Single.just(true));

        credentialsChecker.checkExistsOneByProviderId(accountId, providerId)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void checkExistsOneByProviderIdFalse(VertxTestContext testContext) {
        long accountId = 1L, providerId = 2L;

        when(credentialsService.existsOneByAccountIdAndProviderId(accountId, providerId))
            .thenReturn(Single.just(false));

        credentialsChecker.checkExistsOneByProviderId(accountId, providerId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                    testContext.completeNow();
                })
            );
    }
}
