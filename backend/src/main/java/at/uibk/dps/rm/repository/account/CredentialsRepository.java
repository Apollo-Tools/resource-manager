package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the credentials entity.
 *
 * @author matthi-g
 */
@Deprecated
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
     * @param session the db session
     * @param id the id of the credentials
     * @return a CompletionStage that emits the credentials if they exist, else null
     */
    public CompletionStage<Credentials> findByIdAndFetch(Session session, long id) {
        return session.createQuery(
                "from Credentials c left join fetch c.resourceProvider where c.credentialsId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull();
    }

    /**
     * Find credentials by their credentialsId and account credentialsId
     *
     * @param session the db session
     * @param credentialsId the id of the credentials
     * @param accountId the credentialsId of the creator
     * @return a CompletionStage that emits the credentials if they exist, else null
     */
    public CompletionStage<Credentials> findByIdAndAccountId(Session session, long credentialsId, long accountId) {
        return session.createQuery("select distinct ac.credentials from AccountCredentials ac " +
                "where ac.credentials.credentialsId=:credentialsId and ac.account.accountId=:accountId",entityClass)
            .setParameter("credentialsId", credentialsId)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull();
    }

    /**
     * Find all credentials by their account.
     *
     * @param session the database session
     * @param accountId the id of the account
     * @return a CompletionStage that emits a list of existing credentials
     */
    public CompletionStage<List<Credentials>> findAllByAccountId(Session session, long accountId) {
        return session.createQuery("select c from AccountCredentials ac " +
                "left join ac.credentials c " +
                "left join fetch c.resourceProvider rp " +
                "where ac.account.accountId=:accountId " +
                "order by rp.provider", entityClass)
            .setParameter("accountId", accountId)
            .getResultList();
    }

    /**
     * Find credentials by their account and resource provider.
     *
     * @param session the database session
     * @param accountId the id of the account
     * @param providerId the id of the provider
     * @return a CompletionStage that emits the credentials if they exist, ele null
     */
    public CompletionStage<Credentials> findByAccountIdAndProviderId(Session session, long accountId, long providerId) {
        return session.createQuery("select ac.credentials from AccountCredentials ac " +
                "where ac.account.accountId=:accountId and ac.credentials.resourceProvider.providerId=:providerId",
                entityClass)
            .setParameter("accountId", accountId)
            .setParameter("providerId", providerId)
            .getSingleResultOrNull();
    }
}
