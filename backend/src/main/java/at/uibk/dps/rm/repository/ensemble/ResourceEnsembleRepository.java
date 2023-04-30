package at.uibk.dps.rm.repository.ensemble;

import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class ResourceEnsembleRepository extends Repository<ResourceEnsemble> {
    public ResourceEnsembleRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ResourceEnsemble.class);
    }

    public CompletionStage<List<ResourceEnsemble>> findAllByEnsembleId(long ensembleId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct re from ResourceEnsemble re " +
                    "left join fetch re.resource r " +
                    "left join fetch r.resourceType rt " +
                    "left join fetch r.region reg " +
                    "left join fetch reg.resourceProvider " +
                    "left join fetch r.metricValues mv " +
                    "left join fetch mv.metric " +
                    "where re.ensemble.ensembleId=:ensembleId", entityClass)
                .setParameter("ensembleId", ensembleId)
                .getResultList()
        );
    }
}
