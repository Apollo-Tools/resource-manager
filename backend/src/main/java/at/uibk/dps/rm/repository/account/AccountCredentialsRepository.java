package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.AccountCredentials;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the account_credentials entity.
 *
 * @author matthi-g
 */
public class AccountCredentialsRepository extends Repository<AccountCredentials> {

    /**
     * Create an instance.
     */
    public AccountCredentialsRepository() {
        super(AccountCredentials.class);
    }

    /**
     * Find account credentials by the accountId and providerId.
     *
     * @param session the database session
     * @param accountId the id of the credentials owner
     * @param providerId the id of the resource provider
     * @return a CompletionStage that emits the account credentials if they exist, else null
     */
    public CompletionStage<AccountCredentials> findByAccountAndProvider(Session session, long accountId,
            long providerId) {
        return session.createQuery("from AccountCredentials ac " +
                "where ac.account.accountId=:accountId and " +
                "ac.credentials.resourceProvider.providerId=:providerId", entityClass)
            .setParameter("accountId", accountId)
            .setParameter("providerId", providerId)
            .getSingleResultOrNull();
    }

    /**
     * Find account credentials by the credentialsId and accountId.
     *
     * @param session the database session
     * @param credentialsId the id of the credentials
     * @param accountId the id of the credentials owner
     * @return a CompletionStage that emits the account credentials if they exist, else null
     */
    public CompletionStage<AccountCredentials> findByCredentialsAndAccount(Session session, long credentialsId,
        long accountId) {
        return session.createQuery("from AccountCredentials ac " +
                "left join fetch ac.credentials " +
                "left join fetch ac.account " +
                "where ac.credentials.credentialsId=:credentialsId and ac.account.accountId=:accountId", entityClass)
            .setParameter("credentialsId", credentialsId)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull();
    }
}
