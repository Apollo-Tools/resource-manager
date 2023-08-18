package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.configuration.JWTAuthProvider;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.auth.jwt.JWTAuth;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.List;

/**
 * Processes the http requests that concern the account entity.
 *
 * @author matthi-g
 */
public class AccountHandler extends ValidationHandler {

    private final AccountChecker accountChecker;

    private final JWTAuth jwtAuth;

  /**
   * Create an instance from the accountChecker and jwtAuth.
   *
   * @param accountChecker the account checker
   * @param jwtAuth the jwtAuth instance to create access tokens
   */
    public AccountHandler(AccountChecker accountChecker, JWTAuth jwtAuth) {
        super(accountChecker);
        this.accountChecker = accountChecker;
        this.jwtAuth = jwtAuth;
    }

    /**
     * Get the details of a user account by either using the path parameter id or the id of the
     * logged-in user.
     *
     * @param rc the routing context
     * @param usePrincipal whether to use the id of the logged-in user or path parameter
     * @return details of the user account
     */
    public Single<JsonObject> getOne(RoutingContext rc, boolean usePrincipal) {
        Single<Long> getAccountId;
        if (usePrincipal) {
            getAccountId = Single.just(rc.user().principal().getLong("account_id"));
        } else {
            getAccountId = HttpHelper.getLongPathParam(rc, "id");
        }
        return getAccountId.flatMap(entityChecker::checkFindOne)
            .map(result -> {
                result.remove("password");
                return result;
            });
    }

    @Override
    public Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        JsonObject principal = rc.user().principal();
        return entityChecker.submitUpdate(principal.getLong("account_id"), requestBody);
    }

  /**
   * Process the login request.
   *
   * @param rc the routing context
   * @return the generated access token
   */
  public Single<JsonObject> login(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return accountChecker.checkLoginAccount(requestBody.getString("username"),
                requestBody.getString("password"))
            .map(result -> {
                result.remove("password");
                String role = result.getJsonObject("role").getString("role");
                result.put(JWTAuthProvider.ROLE_CLAIM, List.of(role));
                return new JsonObject().put("token", jwtAuth.generateToken(result));
            });
    }
}
