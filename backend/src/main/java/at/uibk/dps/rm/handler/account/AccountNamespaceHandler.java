package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the namespaces linked to the account entity.
 *
 * @author matthi-g
 */
public class AccountNamespaceHandler extends ValidationHandler {

    private final AccountNamespaceChecker checker;

    /**
     * Create an instance from the accountNamespaceChecker.
     *
     * @param accountNamespaceChecker the account namespace checker
     */
    public AccountNamespaceHandler(AccountNamespaceChecker accountNamespaceChecker) {
        super(accountNamespaceChecker);
        this.checker = accountNamespaceChecker;
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "accountId")
            .flatMap(accountId -> HttpHelper.getLongPathParam(rc, "namespaceId")
                .flatMap(namespaceId -> checker.submitCreate(accountId, namespaceId))
            );
    }

    @Override
    protected Completable deleteOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "accountId")
            .flatMapCompletable(accountId -> HttpHelper.getLongPathParam(rc, "namespaceId")
                .flatMapCompletable(namespaceId -> checker.submitDelete(accountId, namespaceId))
            );
    }
}
