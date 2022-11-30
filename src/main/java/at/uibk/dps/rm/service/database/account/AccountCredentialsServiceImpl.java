package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.AccountCredentials;
import at.uibk.dps.rm.repository.AccountCredentialsRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;

public class AccountCredentialsServiceImpl extends ServiceProxy<AccountCredentials> implements  AccountCredentialsService {
    public AccountCredentialsServiceImpl(AccountCredentialsRepository repository) {
        super(repository, AccountCredentials.class);
    }
}
