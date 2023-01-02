package at.uibk.dps.rm.handler.account;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.regex.Pattern;

public class AccountInputHandler {

    public static void validateSignupRequest(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        validatePassword(requestBody.getString("password"))
            .andThen(validateUsername(requestBody.getString("username")))
            .subscribe(rc::next, throwable -> rc.fail(400, throwable))
            .dispose();
    }

    public static void validateChangePasswordRequest(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        validatePassword(requestBody.getString("new_password"))
            .subscribe(rc::next, throwable -> rc.fail(400, throwable))
            .dispose();
    }

    private static Completable validatePassword(String password) {
        return Maybe.just(password)
            .map(givenPassword -> {
                /* Src: https://www.geeksforgeeks.org/how-to-validate-a-password-using-regular-expressions-in-java/
                 * At least digit
                 * At least one lower case character
                 * At least one upper case character
                 * At least one special character
                 * No white spaces
                 * between 8 and 512 symbols
                 */
                String regex = "^(?=.*[0-9])" +
                    "(?=.*[\\p{javaLowerCase}])" +
                    "(?=.*[\\p{javaUpperCase}])" +
                    "(?=.*[\\p{Punct}])" +
                    "(?=\\S+$)" +
                    ".{8,512}$";
                Pattern p = Pattern.compile(regex);

                if (!p.matcher(givenPassword).matches()) {
                    throw new Throwable("password does not meet the minimum requirements");
                }
                return password;
            })
            .ignoreElement();
    }

    private static Completable validateUsername(String username) {
        return Maybe.just(username)
            .map(givenUsername -> {
                /* Src: https://www.geeksforgeeks.org/how-to-validate-a-password-using-regular-expressions-in-java/
                 * At least one character, digit or underscore
                 */
                String regex = "^[a-z_0-9]+$";
                Pattern p = Pattern.compile(regex);

                if (!p.matcher(username).matches()) {
                    throw new Throwable("password does not meet the minimum requirements");
                }
                return username;
            })
            .ignoreElement();
    }
}
