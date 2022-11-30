package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.Account;
import org.hibernate.reactive.stage.Stage;

public class AccountRepository  extends Repository<Account> {
    public AccountRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Account.class);
    }
}
