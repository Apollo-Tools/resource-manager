package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.Account;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

public class AccountRepository  extends Repository<Account> {
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

    public CompletionStage<Account> findByUsername(String username) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from Account a where a.username=:username and a.isActive=true", entityClass)
                .setParameter("username", username)
                .getSingleResultOrNull()
        );
    }

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
