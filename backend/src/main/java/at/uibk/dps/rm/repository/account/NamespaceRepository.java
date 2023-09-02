package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.K8sNamespace;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the k8s_namespace entity.
 *
 * @author matthi-g
 */
@Deprecated
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
     * @param session the database session
     * @return a CompletionStage that emits a list of all namespaces
     */
    public CompletionStage<List<K8sNamespace>> findAllAndFetch(Session session) {
        return session.createQuery("select distinct n from K8sNamespace n " +
                "left join fetch n.resource", entityClass)
            .getResultList();
    }

    /**
     * Find all namespaces and fetch the resource.
     *
     * @param session the database session
     * @return a CompletionStage that emits a list of all namespaces
     */
    public CompletionStage<List<K8sNamespace>> findAllByAccountIdAndFetch(Session session, long accountId) {
        return session.createQuery("select distinct n from AccountNamespace an " +
                "left join an.namespace n " +
                "left join fetch n.resource " +
                "where an.account.accountId=:accountId", entityClass)
            .setParameter("accountId", accountId)
            .getResultList();
    }

    /**
     * Find all namespaces and fetch the resource.
     *
     * @param session the database session
     * @return a CompletionStage that emits a list of all namespaces
     */
    public CompletionStage<List<K8sNamespace>> findAllByClusterName(Session session, String clusterName) {
        return session.createQuery("select distinct n from K8sNamespace n " +
                "where n.resource.name=:clusterName", entityClass)
            .setParameter("clusterName", clusterName)
            .getResultList();
    }
}
