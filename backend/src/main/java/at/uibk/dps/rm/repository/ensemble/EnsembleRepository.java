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

    public CompletionStage<Ensemble> findByNameAndAccountId(String name, long accountId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select e from Ensemble e " +
                    "where e.name=:name and e.createdBy.accountId=:accountId", entityClass)
                .setParameter("name", name)
                .setParameter("accountId", accountId)
                .getSingleResultOrNull()
        );
    }

    public CompletionStage<Integer> updateValidity(long ensembleId, boolean isValid) {
        return sessionFactory.withSession(session ->
            session.createQuery("update Ensemble e " +
                    "set isValid=:isValid " +
                    "where e.ensembleId=:ensembleId")
                .setParameter("isValid", isValid)
                .setParameter("ensembleId", ensembleId)
                .executeUpdate()
        );
    }
}
