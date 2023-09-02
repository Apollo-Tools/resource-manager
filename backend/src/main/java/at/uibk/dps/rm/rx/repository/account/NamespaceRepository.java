package at.uibk.dps.rm.rx.repository.account;

import at.uibk.dps.rm.entity.model.K8sNamespace;
import at.uibk.dps.rm.rx.repository.Repository;
import at.uibk.dps.rm.rx.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * Implements database operations for the k8s_namespace entity.
 *
 * @author matthi-g
 */
public class NamespaceRepository extends Repository<K8sNamespace> {

    /**
     * Create an instance.
     */
    public NamespaceRepository() {
        super(K8sNamespace.class);
    }

    /**
     * Find all namespaces and fetch the resource.
     *
     * @param sessionManager the database session manager
     * @return a Single that emits a list of all namespaces
     */
    public Single<List<K8sNamespace>> findAllAndFetch(SessionManager sessionManager) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct n from K8sNamespace n " +
                "left join fetch n.resource", entityClass)
            .getResultList()
        );
    }

    /**
     * Find all namespaces and fetch the resource.
     *
     * @param sessionManager the database session manager
     * @return a Single that emits a list of all namespaces
     */
    public Single<List<K8sNamespace>> findAllByAccountIdAndFetch(SessionManager sessionManager,
            long accountId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct n from AccountNamespace an " +
                "left join an.namespace n " +
                "left join fetch n.resource " +
                "where an.account.accountId=:accountId", entityClass)
            .setParameter("accountId", accountId)
            .getResultList()
        );
    }

    /**
     * Find all namespaces and fetch the resource.
     *
     * @param sessionManager the database session manager
     * @return a Single that emits a list of all namespaces
     */
    public Single<List<K8sNamespace>> findAllByClusterName(SessionManager sessionManager,
            String clusterName) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct n from K8sNamespace n " +
                "where n.resource.name=:clusterName", entityClass)
            .setParameter("clusterName", clusterName)
            .getResultList()
        );
    }
}
