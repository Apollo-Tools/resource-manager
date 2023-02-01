package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class CredentialsRepository  extends Repository<Credentials> {
    public CredentialsRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Credentials.class);
    }

    public CompletionStage<Credentials> findByIdAndFetch(long id) {
        return sessionFactory.withSession(session -> session.createQuery(
                "from Credentials c left join fetch c.resourceProvider where c.credentialsId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull()
        );
    }

    public CompletionStage<List<Credentials>> findAllByAccountId(long accountId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select c from AccountCredentials ac " +
                    "left join ac.credentials c " +
                    "left join fetch c.resourceProvider " +
                    "where ac.account.accountId=:accountId", entityClass)
                .setParameter("accountId", accountId)
                .getResultList()
        );
    }
}
