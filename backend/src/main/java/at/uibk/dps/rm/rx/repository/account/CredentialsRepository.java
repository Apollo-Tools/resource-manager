package at.uibk.dps.rm.rx.repository.account;

import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.rx.repository.Repository;
import at.uibk.dps.rm.rx.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * Implements database operations for the credentials entity.
 *
 * @author matthi-g
 */
public class CredentialsRepository  extends Repository<Credentials> {

    /**
     * Create an instance.
     */
    public CredentialsRepository() {
        super(Credentials.class);
    }

    /**
     * Find credentials by their id and fetch the resource provider
     *
     * @param sessionManager the database session manager
     * @param id the id of the credentials
     * @return a Maybe that emits the credentials if they exist, else null
     */
    public Maybe<Credentials> findByIdAndFetch(SessionManager sessionManager, long id) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery(
                "from Credentials c left join fetch c.resourceProvider where c.credentialsId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find credentials by their credentialsId and account credentialsId
     *
     * @param sessionManager the database session manager
     * @param credentialsId the id of the credentials
     * @param accountId the credentialsId of the creator
     * @return a Maybe that emits the credentials if they exist, else null
     */
    public Maybe<Credentials> findByIdAndAccountId(SessionManager sessionManager, long credentialsId, long accountId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct ac.credentials from " +
                "AccountCredentials" +
                " ac " +
                "where ac.credentials.credentialsId=:credentialsId and ac.account.accountId=:accountId",entityClass)
            .setParameter("credentialsId", credentialsId)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find all credentials by their account.
     *
     * @param sessionManager the database session manager
     * @param accountId the id of the account
     * @return a Single that emits a list of existing credentials
     */
    @Override
    public Single<List<Credentials>> findAllByAccountId(SessionManager sessionManager, long accountId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select c from AccountCredentials ac " +
                "left join ac.credentials c " +
                "left join fetch c.resourceProvider rp " +
                "where ac.account.accountId=:accountId " +
                "order by rp.provider", entityClass)
            .setParameter("accountId", accountId)
            .getResultList()
        );
    }

    /**
     * Find credentials by their account and resource provider.
     *
     * @param sessionManager the database session manager
     * @param accountId the id of the account
     * @param providerId the id of the provider
     * @return a Maybe that emits the credentials if they exist, ele null
     */
    public Maybe<Credentials> findByAccountIdAndProviderId(SessionManager sessionManager, long accountId,
            long providerId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("select ac.credentials from " +
                    "AccountCredentials ac " +
                    "where ac.account.accountId=:accountId and ac.credentials.resourceProvider.providerId=:providerId",
                entityClass)
            .setParameter("accountId", accountId)
            .setParameter("providerId", providerId)
            .getSingleResultOrNull()
        );
    }
}
