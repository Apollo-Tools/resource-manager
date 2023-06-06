package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the credentials entity.
 *
 * @author matthi-g
 */
public class CredentialsRepository  extends Repository<Credentials> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public CredentialsRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Credentials.class);
    }

    /**
     * Find credentials by their id and fetch the resource provider
     *
     * @param id the id of the credentials
     * @return a CompletionStage that emits the credentials if they exist, else null
     */
    public CompletionStage<Credentials> findByIdAndFetch(long id) {
        return sessionFactory.withSession(session -> session.createQuery(
                "from Credentials c left join fetch c.resourceProvider where c.credentialsId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find all credentials by their account.
     *
     * @param accountId the id of the account
     * @return a CompletionStage that emits a list of existing credentials
     */
    public CompletionStage<List<Credentials>> findAllByAccountId(long accountId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select c from AccountCredentials ac " +
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
     * @param accountId the id of the account
     * @param providerId the id of the provider
     * @return a CompletionStage that emits the credentials if they exist, ele null
     */
    public CompletionStage<Credentials> findByAccountIdAndProviderId(long accountId, long providerId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select ac.credentials from AccountCredentials ac " +
                "where ac.account.accountId=:accountId and ac.credentials.resourceProvider.providerId=:providerId",
                    entityClass)
                .setParameter("accountId", accountId)
                .setParameter("providerId", providerId)
                .getSingleResultOrNull()
        );
    }
}
