package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.AccountCredentials;
import org.hibernate.reactive.stage.Stage;

public class AccountCredentialsRepository extends Repository<AccountCredentials> {
    public AccountCredentialsRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, AccountCredentials.class);
    }
}
