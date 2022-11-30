package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.repository.AccountRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;

public class AccountServiceImpl  extends ServiceProxy<Account> implements  AccountService {
    public AccountServiceImpl(AccountRepository repository) {
        super(repository, Account.class);
    }
}
