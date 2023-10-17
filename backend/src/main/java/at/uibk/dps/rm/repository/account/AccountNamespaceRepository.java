package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.AccountNamespace;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;

/**
 * Implements database operations for the account_namespace entity.
 *
 * @author matthi-g
 */
public class AccountNamespaceRepository extends Repository<AccountNamespace> {
    /**
     * Create an instance.
     */
    public AccountNamespaceRepository() {
        super(AccountNamespace.class);
    }

    /**
     * Find an account namespace by its account and namespace.
     *
     * @param sessionManager the database session manager
     * @param accountId the id of the account
     * @param namespaceId the id of the namespace
     * @return a Maybe that emits the account namespace if it exists, else null
     */
    public Maybe<AccountNamespace> findByAccountIdAndNamespaceId(SessionManager sessionManager, long accountId,
            long namespaceId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from AccountNamespace an " +
                "where an.account.accountId=:accountId and an.namespace.namespaceId=:namespaceId", entityClass)
            .setParameter("accountId", accountId)
            .setParameter("namespaceId", namespaceId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find an account namespaces by its account and resource.
     *
     * @param sessionManager the database session manager
     * @param accountId the id of the account
     * @param resourceId the id of the resource
     * @return a Single that emits a list of account namespaces
     */
    public Maybe<AccountNamespace> findByAccountIdAndResourceId(SessionManager sessionManager, long accountId,
                                                                       long resourceId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from AccountNamespace an " +
                "where an.account.accountId=:accountId and an.namespace.resource.resourceId=:resourceId", entityClass)
            .setParameter("accountId", accountId)
            .setParameter("resourceId", resourceId)
            .getSingleResultOrNull()
        );
    }
}
