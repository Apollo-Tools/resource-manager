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

    public CompletionStage<ResourceEnsemble> findByEnsembleIdAndResourceId(long ensembleId, long resourceId) {
        return sessionFactory.withSession(session ->
            session.createQuery("from ResourceEnsemble re " +
                    "where re.ensemble.ensembleId=:ensembleId and re.resource.resourceId=:resourceId", entityClass)
                .setParameter("ensembleId", ensembleId)
                .setParameter("resourceId", resourceId)
                .getSingleResultOrNull());
    }

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
