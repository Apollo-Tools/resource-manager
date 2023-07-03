package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link VPCHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class VPCHandlerTest {

    private VPCHandler vpcHandler;

    @Mock
    private VPCChecker vpcChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        vpcHandler = new VPCHandler(vpcChecker);
    }


    @Test
    void postOneValid(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(1L);
        JsonObject requestBody = new JsonObject("{\n" +
            "  \"region\": {\n" +
            "    \"region_id\": 1\n" +
            "  },\n" +
            "  \"vpc_id_value\": \"vpc-052a2a7bbab80750f\",\n" +
            "  \"subnet_id_value\": \"subnet-0b65e75059518eec0\"\n" +
            "}");
        Account createdBy = new Account();
        createdBy.setAccountId(1L);
        JsonObject createEntity = requestBody.copy().put("created_by", JsonObject.mapFrom(createdBy));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(vpcChecker.submitCreate(createEntity)).thenReturn(Single.just(createEntity));

        vpcHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getString("vpc_id_value")).isEqualTo("vpc-052a2a7bbab80750f");
                    assertThat(result.getString("subnet_id_value")).isEqualTo("subnet-0b65e75059518eec0");
                    assertThat(result.getJsonObject("created_by").getLong("account_id"))
                        .isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void postOneRegionNotExists(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(1L);
        JsonObject requestBody = new JsonObject("{\n" +
            "  \"region\": {\n" +
            "    \"region_id\": 1\n" +
            "  },\n" +
            "  \"vpc_id_value\": \"vpc-052a2a7bbab80750f\",\n" +
            "  \"subnet_id_value\": \"subnet-0b65e75059518eec0\"\n" +
            "}");

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);

        vpcHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> Assertions.fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
            }));
    }

    @Test
    void postOneDuplicate(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(1L);
        JsonObject requestBody = new JsonObject("{\n" +
            "  \"region\": {\n" +
            "    \"region_id\": 1\n" +
            "  },\n" +
            "  \"vpc_id_value\": \"vpc-052a2a7bbab80750f\",\n" +
            "  \"subnet_id_value\": \"subnet-0b65e75059518eec0\"\n" +
            "}");

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);

        vpcHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> Assertions.fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
            }));
    }

}
