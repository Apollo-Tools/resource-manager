package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link CredentialsHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class CredentialsHandlerTest {

    private CredentialsHandler credentialsHandler;

    @Mock
    private CredentialsService credentialsService;

    @Mock
    private RoutingContext rc;

    private long accountId;
    private Account account;
    private Credentials c1, c2;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        credentialsHandler = new CredentialsHandler(credentialsService);
        accountId = 1L;
        account = TestAccountProvider.createAccount(accountId);
        ResourceProvider rp1 = TestResourceProviderProvider.createResourceProvider(1L, "AWS");
        ResourceProvider rp2 = TestResourceProviderProvider.createResourceProvider(2L, "IBM");
        c1 = TestAccountProvider.createCredentials(1L, rp1);
        c2 = TestAccountProvider.createCredentials(2L, rp2);
    }

    @Test
    void getAllFromAccount(VertxTestContext testContext) {
        JsonArray jsonCredentials = new JsonArray(List.of(JsonObject.mapFrom(c1), JsonObject.mapFrom(c2)));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(credentialsService.findAllByAccountIdAndIncludeExcludeSecrets(accountId, false))
            .thenReturn(Single.just(jsonCredentials));

        credentialsHandler.getAllFromAccount(rc)
            .subscribe(result -> {
                    assertThat(result.getJsonObject(0).getLong("credentials_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("credentials_id")).isEqualTo(2L);
                    testContext.completeNow();
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
