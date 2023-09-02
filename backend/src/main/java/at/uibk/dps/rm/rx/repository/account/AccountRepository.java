package at.uibk.dps.rm.rx.repository.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.rx.repository.Repository;
import at.uibk.dps.rm.rx.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;

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
    public Maybe<Account> findById(SessionManager sessionManager, long id) {
        return Maybe.fromCompletionStage(
            sessionManager.getSession()
                .createQuery("from Account a where a.accountId=:id and a.isActive=true", entityClass)
                .setParameter("id", id)
                .getSingleResultOrNull()
        );
    }

    /**
     * Find an account by its username
     *
     * @param sessionManager the database session manager
     * @param username the username of the account
     * @return a Maybe that emits the account if it exists, else null
     */
    public Maybe<Account> findByUsername(SessionManager sessionManager, String username) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from Account a where a.username=:username",
                entityClass)
            .setParameter("username", username)
            .getSingleResultOrNull()
        );
    }
}
