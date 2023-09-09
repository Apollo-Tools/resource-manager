package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.AccountNamespace;
import at.uibk.dps.rm.repository.account.AccountNamespaceRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * The interface of the service proxy for the account_namespace entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface AccountNamespaceService extends DatabaseServiceInterface {
    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static AccountNamespaceService create(SessionManagerProvider smProvider) {
        return new AccountNamespaceServiceImpl(new AccountNamespaceRepository(), smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static AccountNamespaceService createProxy(Vertx vertx) {
        return new AccountNamespaceServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(AccountNamespace.class));
    }

    /**
     * Save a new account namespace by its account and namespace.
     *
     * @param accountId the id of the account
     * @param namespaceId the id of the namespace
     * @param resultHandler receives the persisted entity as JsonObject if the saving was
     *                      successful else an error
     */
    void saveByAccountIdAndNamespaceId(long accountId, long namespaceId,
        Handler<AsyncResult<JsonObject>> resultHandler);

    /**
     * Delete an account namespace by its account and namespace.
     *
     * @param accountId the id of the creator
     * @param namespaceId the id of the namespace
     * @param resultHandler receives nothing if the deletion was successful else an error
     */
    void deleteByAccountIdAndNamespaceId(long accountId, long namespaceId,
        Handler<AsyncResult<Void>> resultHandler);
}
