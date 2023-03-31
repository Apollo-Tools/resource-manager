package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
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
    void getAll(VertxTestContext testContext) {
        long accountId = 1L;
        Credentials entity1 = TestAccountProvider.createCredentials(1L, new ResourceProvider());
        Credentials entity2 = TestAccountProvider.createCredentials(2L, new ResourceProvider());
        List<JsonObject> entities = new ArrayList<>();
        entities.add(JsonObject.mapFrom(entity1));
        entities.add(JsonObject.mapFrom(entity2));
        JsonArray resultJson = new JsonArray(entities);

        when(credentialsService.findAllByAccountId(accountId)).thenReturn(Single.just(resultJson));

        credentialsChecker.checkFindAll(accountId)
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
        long accountId = 1L;
        List<JsonObject> entities = new ArrayList<>();
        JsonArray resultJson = new JsonArray(entities);

        when(credentialsService.findAllByAccountId(accountId)).thenReturn(Single.just(resultJson));

        credentialsChecker.checkFindAll(accountId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkExistsOneByProviderIdTrue(VertxTestContext testContext) {
        long accountId = 1L, providerId = 2L;

        when(credentialsService.existsOnyByAccountIdAndProviderId(accountId, providerId))
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

        when(credentialsService.existsOnyByAccountIdAndProviderId(accountId, providerId))
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
