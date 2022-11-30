package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.Credentials;
import org.hibernate.reactive.stage.Stage;

public class CredentialsRepository  extends Repository<Credentials> {
    public CredentialsRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Credentials.class);
    }
}
