package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.auth.jwt.JWTAuth;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class AccountHandler extends ValidationHandler {

    private final AccountChecker accountChecker;

    private final JWTAuth jwtAuth;

    public AccountHandler(AccountService accountService, JWTAuth jwtAuth) {
        super(new AccountChecker(accountService));
        accountChecker = (AccountChecker) super.entityChecker;
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
    protected Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return entityChecker.checkForDuplicateEntity(requestBody)
            .andThen(Single.defer(() -> Single.just(1L)))
            .map(result -> accountChecker.hashAccountPassword(requestBody))
            .flatMap(entityChecker::submitCreate)
            .map(result -> {
                result.remove("password");
                return result;
            });
    }

    public Single<JsonObject> login(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return accountChecker.checkFindLoginAccount(requestBody.getString("username"))
            .map(account -> accountChecker
                .checkComparePasswords(account,
                    requestBody.getString("password").toCharArray()))
            .map(result -> {
                result.remove("password");
                return new JsonObject().put("token", jwtAuth.generateToken(result));
            });
    }
}
