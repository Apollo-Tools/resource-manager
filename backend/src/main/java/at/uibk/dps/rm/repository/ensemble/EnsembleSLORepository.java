package at.uibk.dps.rm.repository.ensemble;

import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class EnsembleSLORepository extends Repository<EnsembleSLO> {

    public EnsembleSLORepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, EnsembleSLO.class);
    }

    public CompletionStage<List<EnsembleSLO>> findAllAndFetch() {
        return sessionFactory.withSession(session ->
                session.createQuery("select distinct slo from EnsembleSLO slo " +
                                "left join fetch slo.ensemble ensemble ", entityClass)
                        .getResultList()
        );
    }

    public CompletionStage<List<EnsembleSLO>> findAllByEnsembleId(long ensembleId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct slo from EnsembleSLO slo " +
                    "where slo.ensemble.ensembleId=:ensembleId", entityClass)
                .setParameter("ensembleId", ensembleId)
                .getResultList()
        );
    }
}
