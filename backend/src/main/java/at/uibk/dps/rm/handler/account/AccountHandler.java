package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.ValidationHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.auth.jwt.JWTAuth;
import io.vertx.rxjava3.ext.web.RoutingContext;

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

    @Override
    protected Single<JsonObject> getOne(RoutingContext rc) {
        JsonObject user = rc.user().principal();
        return entityChecker.checkFindOne(user.getLong("account_id"))
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
                return new JsonObject().put("token", jwtAuth.generateToken(result));
            });
    }
}
