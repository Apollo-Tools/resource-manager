package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.service.rxjava3.database.service.ServiceService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestServiceProvider;
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
 * Implements tests for the {@link ServiceHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceHandlerTest {

    private ServiceHandler serviceHandler;

    @Mock
    private ServiceService service;

    @Mock
    private RoutingContext rc;

    private long accountId;
    private Account account;
    private Service s1, s2;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        serviceHandler = new ServiceHandler(service);
        accountId = 10L;
        account = TestAccountProvider.createAccount(accountId);
        s1 = TestServiceProvider.createService(1L);
        s2 = TestServiceProvider.createService(2L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"all", "own"})
    void getAll(String type, VertxTestContext testContext) {
        JsonArray jsonResult = new JsonArray(List.of(JsonObject.mapFrom(s1), JsonObject.mapFrom(s2)));
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        if (type.equals("all")) {
            when(service.findAllAccessibleServices(accountId)).thenReturn(Single.just(jsonResult));
        } else {
            when(service.findAllByAccountId(accountId)).thenReturn(Single.just(jsonResult));
        }

        Single<JsonArray> handler = type.equals("all") ? serviceHandler.getAll(rc) :
            serviceHandler.getAllFromAccount(rc);

        handler.blockingSubscribe(result -> testContext.verify(() -> {
                assertThat(result.getJsonObject(0).getLong("service_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("service_id")).isEqualTo(2L);
                testContext.completeNow();
            }),
            throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
    }
}
