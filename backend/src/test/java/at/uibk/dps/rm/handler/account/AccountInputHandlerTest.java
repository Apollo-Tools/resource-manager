package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Implements tests for the {@link AccountInputHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class AccountInputHandlerTest {

    @Mock
    RoutingContext rc;

    @ParameterizedTest
    @ValueSource(strings = {"johndoe", "john_doe", "john123", "john_doe1234", "j", "1", "_"})
    void validateSignupRequestValidUsername(String username, VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\n" +
            "  \"username\": \"" + username + "\",\n" +
            "  \"password\": \"Password1!\"\n" +
            "}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);

        AccountInputHandler.validateSignupRequest(rc);

        verify(rc).next();
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1aA#aaaa", "11111aA#", "BBBBBc4?", "!$%&hIfefaw4fafaewf"})
    void validateSignupRequestValidPassword(String password, VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\n" +
            "  \"username\": \"johndoe\",\n" +
            "  \"password\": \"" + password + "\"\n" +
            "}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);

        AccountInputHandler.validateSignupRequest(rc);

        verify(rc).next();
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Johndoe", "john,doe", "jöhn", "jähn", "&"})
    void validateSignupRequestInvalidUsername(String username, VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\n" +
            "  \"username\": \"" + username + "\",\n" +
            "  \"password\": \"Password1!\"\n" +
            "}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);

        AccountInputHandler.validateSignupRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1aA#aaa", "11111a1#", "BBBBBG4?", "Ifefaw4fafaewf"})
    void validateSignupRequestInvalidPassword(String password, VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\n" +
            "  \"username\": \"johndoe\",\n" +
            "  \"password\": \"" + password + "\"\n" +
            "}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);

        AccountInputHandler.validateSignupRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1aA#aaaa", "11111aA#", "BBBBBc4?", "!$%&hIfefaw4fafaewf"})
    void validateChangePasswordRequestValidPassword(String password, VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\n" +
            "  \"old_password\": \"password\",\n" +
            "  \"new_password\": \"" + password + "\"\n" +
            "}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);

        AccountInputHandler.validateChangePasswordRequest(rc);

        verify(rc).next();
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1aA#aaa", "11111a1#", "BBBBBG4?", "Ifefaw4fafaewf"})
    void validateChangePasswordRequestInvalidPassword(String password, VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\n" +
            "  \"old_password\": \"password\",\n" +
            "  \"new_password\": \"" + password + "\"\n" +
            "}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);

        AccountInputHandler.validateChangePasswordRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }
}
