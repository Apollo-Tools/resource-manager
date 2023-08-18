package at.uibk.dps.rm.repository.ensemble;

import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.concurrent.CompletionStage;

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
     * @param session the database session
     * @param accountId the id of the account
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @return a CompletionStage that emits the resource ensemble if it exists, else null
     */
    public CompletionStage<ResourceEnsemble> findByEnsembleIdAndResourceId(Session session, long accountId,
            long ensembleId, long resourceId) {
        return session.createQuery("from ResourceEnsemble re " +
                "where re.ensemble.createdBy.accountId=:accountId and  re.ensemble.ensembleId=:ensembleId and " +
                    "re.resource.resourceId=:resourceId", entityClass)
            .setParameter("accountId", accountId)
            .setParameter("ensembleId", ensembleId)
            .setParameter("resourceId", resourceId)
            .getSingleResultOrNull();
    }
}
