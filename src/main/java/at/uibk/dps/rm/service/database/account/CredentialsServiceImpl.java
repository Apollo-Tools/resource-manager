package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.repository.CredentialsRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;

public class CredentialsServiceImpl  extends ServiceProxy<Credentials> implements  CredentialsService {
    public CredentialsServiceImpl(CredentialsRepository repository) {
        super(repository, Credentials.class);
    }
}
