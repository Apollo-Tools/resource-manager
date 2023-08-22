package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the account entity.
 *
 * @author matthi-g
 */
public class AccountRepository extends Repository<Account> {

    /**
     * Create an instance.
     */
    public AccountRepository() {
        super(Account.class);
    }

    @Override
    public CompletionStage<Account> findById(Session session, long id) {
        return session.createQuery("from Account a where a.accountId=:id and a.isActive=true", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull();
    }

    /**
     * Find an account by its username
     *
     * @param session the db session
     * @param username the username of the account
     * @return a CompletionStage that emits the account if it exists, else null
     */
    public CompletionStage<Account> findByUsername(Session session, String username) {
        return session.createQuery("from Account a where a.username=:username", entityClass)
            .setParameter("username", username)
            .getSingleResultOrNull();
    }
}
