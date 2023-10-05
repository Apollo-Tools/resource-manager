package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.service.rxjava3.database.account.AccountNamespaceService;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link AccountNamespaceHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class AccountNamespaceHandlerTest {

    private AccountNamespaceHandler accountNamespaceHandler;

    @Mock
    private AccountNamespaceService accountNamespaceService;

    @Mock
    private RoutingContext rc;

    private long accountId, namespaceId;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        accountNamespaceHandler = new AccountNamespaceHandler(accountNamespaceService);
        accountId = 1L;
        namespaceId = 2L;
    }

    @Test
    void postOne(VertxTestContext testContext) {
        JsonObject persistedEntity = new JsonObject("{\"account_id\": " + accountId + ", \"namespace_id\": " +
            namespaceId + "}");

        when(rc.pathParam("accountId")).thenReturn(String.valueOf(accountId));
        when(rc.pathParam("namespaceId")).thenReturn(String.valueOf(namespaceId));
        when(accountNamespaceService.saveByAccountIdAndNamespaceId(accountId, namespaceId))
            .thenReturn(Single.just(persistedEntity));

        accountNamespaceHandler.postOne(rc)
            .subscribe(result -> {
                    assertThat(result.getLong("account_id")).isEqualTo(1L);
                    assertThat(result.getLong("namespace_id")).isEqualTo(2L);
                    testContext.completeNow();
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void deleteOne(VertxTestContext testContext) {
        when(rc.pathParam("accountId")).thenReturn(String.valueOf(accountId));
        when(rc.pathParam("namespaceId")).thenReturn(String.valueOf(namespaceId));
        when(accountNamespaceService.deleteByAccountIdAndNamespaceId(accountId, namespaceId))
            .thenReturn(Completable.complete());

        accountNamespaceHandler.deleteOne(rc)
            .subscribe(testContext::completeNow,
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
