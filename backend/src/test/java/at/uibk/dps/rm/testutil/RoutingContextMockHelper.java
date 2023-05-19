package at.uibk.dps.rm.testutil;

import at.uibk.dps.rm.entity.model.Account;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.auth.User;
import io.vertx.rxjava3.ext.web.RequestBody;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.experimental.UtilityClass;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility class with different mocking methods of the {@link RoutingContext}.
 *
 * @author matthi-g
 */
@UtilityClass
public class RoutingContextMockHelper {

    /**
     * Mock the request body of the routing context.
     *
     * @param rc the routing context
     * @param requestBody the request body
     */
    public static void mockBody(RoutingContext rc, JsonObject requestBody) {
        RequestBody mockBody = mock(RequestBody.class);
        when(rc.body()).thenReturn(mockBody);
        when(mockBody.asJsonObject()).thenReturn(requestBody);
    }

    /**
     * Mock the request body of the routing context.
     *
     * @param rc the routing context
     * @param requestBody the request body
     */
    public static void mockBody(RoutingContext rc, JsonArray requestBody) {
        RequestBody mockBody = mock(RequestBody.class);
        when(rc.body()).thenReturn(mockBody);
        when(mockBody.asJsonArray()).thenReturn(requestBody);
    }

    /**
     * Mock the user principal of the routing context.
     *
     * @param rc the routing context
     * @param account the account of the mocked user
     */
    public static void mockUserPrincipal(RoutingContext rc, Account account) {
        JsonObject userPrincipal = new JsonObject();
        userPrincipal.put("username", account.getUsername());
        userPrincipal.put("account_id", account.getAccountId());
        when(rc.user()).thenReturn(User.create(userPrincipal));
    }
}
