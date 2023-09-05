package at.uibk.dps.rm.rx.repository.ensemble;

import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.rx.repository.Repository;
import at.uibk.dps.rm.rx.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;

/**
 * Implements database operations for the resource_ensemble entity.
 *
 * @author matthi-g
 */
public class ResourceEnsembleRepository extends Repository<ResourceEnsemble> {
    /**
     * Create an instance.
     */
    public ResourceEnsembleRepository() {
        super(ResourceEnsemble.class);
    }

    /**
     * Find a resource ensemble by its ensemble and resource.
     *
     * @param sessionManager the database session manager
     * @param accountId the id of the account
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @return a Maybe that emits the resource ensemble if it exists, else null
     */
    public Maybe<ResourceEnsemble> findByEnsembleIdAndResourceId(SessionManager sessionManager, long accountId,
            long ensembleId, long resourceId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from ResourceEnsemble re " +
                "where re.ensemble.createdBy.accountId=:accountId and  re.ensemble.ensembleId=:ensembleId and " +
                    "re.resource.resourceId=:resourceId", entityClass)
            .setParameter("accountId", accountId)
            .setParameter("ensembleId", ensembleId)
            .setParameter("resourceId", resourceId)
            .getSingleResultOrNull()
        );
    }
}
