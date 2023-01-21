package at.uibk.dps.rm.testutil;

import at.uibk.dps.rm.entity.model.Account;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.auth.User;
import io.vertx.rxjava3.ext.web.RequestBody;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RoutingContextMockHelper {

    public static void mockBody(RoutingContext rc, JsonObject jsonObject) {
        RequestBody mockBody = mock(RequestBody.class);
        when(rc.body()).thenReturn(mockBody);
        when(mockBody.asJsonObject()).thenReturn(jsonObject);
    }

    public static void mockBody(RoutingContext rc, JsonArray jsonArray) {
        RequestBody mockBody = mock(RequestBody.class);
        when(rc.body()).thenReturn(mockBody);
        when(mockBody.asJsonArray()).thenReturn(jsonArray);
    }

    public static void mockUserPrincipal(RoutingContext rc, Account account) {
        JsonObject userPrincipal = new JsonObject();
        userPrincipal.put("username", account.getUsername());
        userPrincipal.put("account_id", account.getAccountId());
        when(rc.user()).thenReturn(User.create(userPrincipal));
    }

    public static void mockQueryParam(RoutingContext rc, String key, List<String> values) {
        when(rc.queryParam(key)).thenReturn(values);
    }
}
