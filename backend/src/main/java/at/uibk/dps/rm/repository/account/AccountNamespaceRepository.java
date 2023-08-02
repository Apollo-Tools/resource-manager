package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.AccountNamespace;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.List;
import java.util.concurrent.CompletionStage;

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
     * @param session the database session
     * @param accountId the id of the account
     * @param namespaceId the id of the namespace
     * @return a CompletionStage that emits the account namespace if it exists, else null
     */
    public CompletionStage<AccountNamespace> findByAccountIdAndNamespaceId(Session session, long accountId,
            long namespaceId) {
        return session.createQuery("from AccountNamespace an " +
                "where an.account.accountId=:accountId and an.namespace.namespaceId=:namespaceId", entityClass)
            .setParameter("accountId", accountId)
            .setParameter("namespaceId", namespaceId)
            .getSingleResultOrNull();
    }

    /**
     * Find an account namespaces by it account and resource.
     *
     * @param session the database session
     * @param accountId the id of the account
     * @param resourceId the id of the resource
     * @return a CompletionStage that emits a list of account namespaces
     */
    public CompletionStage<List<AccountNamespace>> findByAccountIdAndResourceId(Session session, long accountId,
            long resourceId) {
        return session.createQuery("from AccountNamespace an " +
                "where an.account.accountId=:accountId and an.namespace.resource.resourceId=:resourceId", entityClass)
            .setParameter("accountId", accountId)
            .setParameter("resourceId", resourceId)
            .getResultList();
    }
}
