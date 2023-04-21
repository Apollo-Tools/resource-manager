package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the account entity.
 *
 * @author matthi-g
 */
public class AccountRepository  extends Repository<Account> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public AccountRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Account.class);
    }

    @Override
    public CompletionStage<Account> findById(long id) {
        return sessionFactory.withSession(session ->
            session.createQuery("from Account a where a.accountId=:id and a.isActive=true", entityClass)
                .setParameter("id", id)
                .getSingleResultOrNull()
        );
    }

    /**
     * Find an account by its username
     *
     * @param username the username of the account
     * @return a CompletionStage that emits the account if it exists, else null
     */
    public CompletionStage<Account> findByUsername(String username) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from Account a where a.username=:username and a.isActive=true", entityClass)
                .setParameter("username", username)
                .getSingleResultOrNull()
        );
    }

    /**
     * @see #findByUsername(String)
     *
     * @param username the username of the account
     * @param isActive whether the account is active or not
     * @return a CompletionStage that emits the account if it exists, else null
     */
    public CompletionStage<Account> findByUsername(String username, boolean isActive) {
        if (isActive) {
            return findByUsername(username);
        }
        return sessionFactory.withSession(session ->
            session.createQuery("from Account a where a.username=:username", entityClass)
                .setParameter("username", username)
                .getSingleResultOrNull()
        );
    }
}
