package at.uibk.dps.rm.testutil;

import at.uibk.dps.rm.entity.model.Account;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.http.HttpServerRequest;
import io.vertx.rxjava3.ext.auth.User;
import io.vertx.rxjava3.ext.web.FileUpload;
import io.vertx.rxjava3.ext.web.RequestBody;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.experimental.UtilityClass;
import org.mockito.Mockito;

import java.util.List;

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
     * Mock the request body of the routing context.
     *
     * @param rc the routing context
     * @param requestBody the request body
     */
    public static void mockBody(RoutingContext rc, Buffer requestBody) {
        RequestBody mockBody = mock(RequestBody.class);
        when(rc.body()).thenReturn(mockBody);
        when(mockBody.buffer()).thenReturn(requestBody);
    }

    /**
     * Mock an empty request body of the routing context.
     *
     * @param rc the routing context
     */
    public static void mockNullBody(RoutingContext rc) {
        when(rc.body()).thenReturn(null);
    }

    /**
     * Mock the form attributes of a multiform request.
     *
     * @param rc the routing context
     * @param attributes the request attributes
     */
    public static void mockFormAttributes(RoutingContext rc, HttpServerRequest request, MultiMap attributes) {
        when(rc.request()).thenReturn(request);
        when(request.formAttributes()).thenReturn(attributes);
    }

    /**
     * Mock the file uploads of a multiform request.
     *
     * @param rc the routing context
     * @param filename the name of the file
     */
    public static void mockFileUpload(RoutingContext rc, String filename) {
        FileUpload fileUpload = mock(FileUpload.class);
        when(rc.fileUploads()).thenReturn(List.of(fileUpload));
        Mockito.lenient().when(fileUpload.uploadedFileName()).thenReturn(filename);
        Mockito.lenient().when(fileUpload.fileName()).thenReturn(filename);
    }

    public static void mockHeaders(RoutingContext rc, HttpServerRequest request, MultiMap headers) {
        when(rc.request()).thenReturn(request);
        when(request.headers()).thenReturn(headers);
    }

    public static void mockHeaders(RoutingContext rc, MultiMap headers) {
        HttpServerRequest request = mock(HttpServerRequest.class);
        when(rc.request()).thenReturn(request);
        when(request.headers()).thenReturn(headers);
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
