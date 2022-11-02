package at.uibk.dps.rm.testutil;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RequestBody;
import io.vertx.rxjava3.ext.web.RoutingContext;

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
}
