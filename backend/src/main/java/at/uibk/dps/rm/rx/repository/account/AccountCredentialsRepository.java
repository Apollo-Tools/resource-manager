package at.uibk.dps.rm.rx.repository.account;

import at.uibk.dps.rm.entity.model.AccountCredentials;
import at.uibk.dps.rm.rx.repository.Repository;
import at.uibk.dps.rm.rx.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;

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
     * @param sessionManager the database session manager
     * @param accountId the id of the credentials owner
     * @param providerId the id of the resource provider
     * @return a Maybe that emits the account credentials if they exist, else null
     */
    public Maybe<AccountCredentials> findByAccountAndProvider(SessionManager sessionManager, long accountId,
            long providerId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from AccountCredentials ac " +
                "where ac.account.accountId=:accountId and " +
                "ac.credentials.resourceProvider.providerId=:providerId", entityClass)
            .setParameter("accountId", accountId)
            .setParameter("providerId", providerId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find account credentials by the credentialsId and accountId.
     *
     * @param sessionManager the database session manager
     * @param credentialsId the id of the credentials
     * @param accountId the id of the credentials owner
     * @return a Maybe that emits the account credentials if they exist, else null
     */
    public Maybe<AccountCredentials> findByCredentialsAndAccount(SessionManager sessionManager,
        long credentialsId, long accountId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from AccountCredentials ac " +
                "left join fetch ac.credentials " +
                "left join fetch ac.account " +
                "where ac.credentials.credentialsId=:credentialsId and ac.account.accountId=:accountId", entityClass)
            .setParameter("credentialsId", credentialsId)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull()
        );
    }
}
