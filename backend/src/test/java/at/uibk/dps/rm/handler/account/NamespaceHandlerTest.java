package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.K8sNamespace;
import at.uibk.dps.rm.service.rxjava3.database.account.NamespaceService;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link NamespaceHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class NamespaceHandlerTest {

    private NamespaceHandler namespaceHandler;

    @Mock
    private NamespaceService namespaceService;

    @Mock
    private RoutingContext rc;

    private long accountId;
    private Account account;
    private K8sNamespace n1, n2;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        namespaceHandler = new NamespaceHandler(namespaceService);
        accountId = 1L;
        account = TestAccountProvider.createAccount(accountId);
        n1 = TestResourceProviderProvider.createNamespace(1L);
        n2 = TestResourceProviderProvider.createNamespace(2L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getAllByAccount(boolean userPrincipal, VertxTestContext testContext) {
        JsonArray jsonNamespaces = new JsonArray(List.of(JsonObject.mapFrom(n1), JsonObject.mapFrom(n2)));

        if (userPrincipal) {
            RoutingContextMockHelper.mockUserPrincipal(rc, account);
        } else {
            when(rc.pathParam("id")).thenReturn(String.valueOf(accountId));
        }
        when(namespaceService.findAllByAccountId(accountId)).thenReturn(Single.just(jsonNamespaces));

        namespaceHandler.getAllByAccount(rc, userPrincipal)
            .subscribe(result -> {
                    assertThat(result.getJsonObject(0).getLong("namespace_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("namespace_id")).isEqualTo(2L);
                    testContext.completeNow();
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
