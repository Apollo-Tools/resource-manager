package at.uibk.dps.rm.repository.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class EnsembleRepository extends Repository<Ensemble> {

    public EnsembleRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Ensemble.class);
    }

    public CompletionStage<List<Ensemble>> findAllByAccountId(long accountId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct e from Ensemble e " +
                    "where e.createdBy.accountId=:accountId " +
                    "order by e.id", entityClass)
                .setParameter("accountId", accountId)
                .getResultList()
        );
    }

    public CompletionStage<Ensemble> findByIdAndAccountId(long id, long accountId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select e from Ensemble e " +
                    "where e.ensembleId=:id and e.createdBy.accountId=:accountId", entityClass)
                .setParameter("id", id)
                .setParameter("accountId", accountId)
                .getSingleResultOrNull()
        );
    }
}
