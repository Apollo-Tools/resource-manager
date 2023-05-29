package at.uibk.dps.rm.repository.ensemble;

import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the resource_ensemble entity.
 *
 * @author matthi-g
 */
public class ResourceEnsembleRepository extends Repository<ResourceEnsemble> {
    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public ResourceEnsembleRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ResourceEnsemble.class);
    }

    /**
     * Find a resource ensemble by its ensemble and resource.
     *
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @return a CompletionStage that emits the resource ensemble if it exists, else null
     */
    public CompletionStage<ResourceEnsemble> findByEnsembleIdAndResourceId(long ensembleId, long resourceId) {
        return sessionFactory.withSession(session ->
            session.createQuery("from ResourceEnsemble re " +
                    "where re.ensemble.ensembleId=:ensembleId and re.resource.resourceId=:resourceId", entityClass)
                .setParameter("ensembleId", ensembleId)
                .setParameter("resourceId", resourceId)
                .getSingleResultOrNull());
    }

    /**
     * Delete a resourc ensemble by its ensemble and resource.
     *
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @return a CompletionStage that emits the row count
     */
    public CompletionStage<Integer> deleteEnsembleIdAndResourceId(long ensembleId, long resourceId) {
        return this.sessionFactory.withTransaction(session ->
            session.createQuery("delete from ResourceEnsemble re " +
                    "where re.ensemble.ensembleId=:ensembleId and re.resource.resourceId=:resourceId")
                .setParameter("ensembleId", ensembleId)
                .setParameter("resourceId", resourceId)
                .executeUpdate()
        );
    }
}
