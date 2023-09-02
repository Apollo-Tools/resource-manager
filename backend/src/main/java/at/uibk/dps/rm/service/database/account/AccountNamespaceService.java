package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.AccountNamespace;
import at.uibk.dps.rm.repository.account.AccountNamespaceRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

/**
 * The interface of the service proxy for the account_namespace entity.
 *
 * @author matthi-g
 */
@Deprecated
@ProxyGen
@VertxGen
public interface AccountNamespaceService extends DatabaseServiceInterface {
    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static AccountNamespaceService create(Stage.SessionFactory sessionFactory) {
        return new AccountNamespaceServiceImpl(new AccountNamespaceRepository(), sessionFactory);
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
     * @return a Future that emits the persisted entity as JsonObject
     */
    Future<JsonObject> saveByAccountIdAndNamespaceId(long accountId, long namespaceId);

    /**
     * Delete a account namespace by its account and namespace.
     *
     * @param accountId the id of the creator
     * @param namespaceId the id of the namespace
     * @return an empty Future
     */
    Future<Void> deleteByAccountIdAndNamespaceId(long accountId, long namespaceId);
}
