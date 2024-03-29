package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.account.NamespaceService;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the k8s_namespace entity.
 *
 * @author matthi-g
 */
public class NamespaceHandler extends ValidationHandler {

    private final NamespaceService namespaceService;

    /**
     * Create an instance from the namespaceService.
     *
     * @param namespaceService the service
     */
    public NamespaceHandler(NamespaceService namespaceService) {
        super(namespaceService);
        this.namespaceService = namespaceService;
    }

    /**
     * List all namespaces of a user account by either using the path parameter id or the id of the
     * logged-in user.
     *
     * @param rc the routing context
     * @param usePrincipal whether to use the id of the logged-in user or path parameter
     * @return all namespaces of the user account
     */
    public Single<JsonArray> getAllByAccount(RoutingContext rc, boolean usePrincipal) {
        Single<Long> getAccountId;
        if (usePrincipal) {
            getAccountId = Single.just(rc.user().principal().getLong("account_id"));
        } else {
            getAccountId = HttpHelper.getLongPathParam(rc, "id");
        }
        return getAccountId.flatMap(namespaceService::findAllByAccountId);
    }
}
