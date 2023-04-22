package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

public class AccountServiceImpl extends DatabaseServiceProxy<Account> implements  AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository repository) {
        super(repository, Account.class);
        accountRepository = repository;
    }

    @Override
    public Future<JsonObject> findOneByUsername(String username) {
        return Future
            .fromCompletionStage(accountRepository.findByUsername(username))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<Boolean> existsOneByUsername(String username, boolean isActive) {
        return Future
            .fromCompletionStage(accountRepository.findByUsername(username, isActive))
            .map(Objects::nonNull);
    }
}
