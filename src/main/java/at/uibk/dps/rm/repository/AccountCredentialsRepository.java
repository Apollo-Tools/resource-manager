package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.AccountCredentials;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

public class AccountCredentialsRepository extends Repository<AccountCredentials> {
    public AccountCredentialsRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, AccountCredentials.class);
    }

    public CompletionStage<AccountCredentials> findByAccountAndProvider(long accountId, long providerId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from AccountCredentials ac " +
                        "where ac.account.accountId=:accountId and " +
                        "ac.credentials.resourceProvider.providerId=:providerId",
                    entityClass)
                .setParameter("accountId", accountId)
                .setParameter("providerId", providerId)
                .getSingleResultOrNull()
        );
    }

    public CompletionStage<AccountCredentials> findByCredentialsAndAccount(long credentialsId, long accountId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from AccountCredentials ac " +
                        "left join fetch ac.credentials " +
                        "left join ac.account " +
                        "where ac.credentials.credentialsId=:credentialsId and ac.account.accountId=:accountId",
                    entityClass)
                .setParameter("credentialsId", credentialsId)
                .setParameter("accountId", accountId)
                .getSingleResultOrNull()
        );
    }
}
