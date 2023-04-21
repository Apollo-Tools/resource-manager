package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.AccountCredentials;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the account_credentials entity.
 *
 * @author matthi-g
 */
public class AccountCredentialsRepository extends Repository<AccountCredentials> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public AccountCredentialsRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, AccountCredentials.class);
    }

    /**
     * Find account credentials by the accountId and providerId.
     *
     * @param accountId the id of the credentials owner
     * @param providerId the id of the resource provider
     * @return a CompletionStage that emits the account credentials if they exist, else null
     */
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

    /**
     * Find account credentials by the credentialsId and accountId.
     *
     * @param credentialsId the id of the credentials
     * @param accountId the id of the credentials owner
     * @return a CompletionStage that emits the account credentials if they exist, else null
     */
    public CompletionStage<AccountCredentials> findByCredentialsAndAccount(long credentialsId, long accountId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from AccountCredentials ac " +
                        "left join fetch ac.credentials " +
                        "left join fetch ac.account " +
                        "where ac.credentials.credentialsId=:credentialsId and ac.account.accountId=:accountId",
                    entityClass)
                .setParameter("credentialsId", credentialsId)
                .setParameter("accountId", accountId)
                .getSingleResultOrNull()
        );
    }
}
