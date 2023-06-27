package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.Objects;

/**
 * This is the implementation of the #AccountService.
 *
 * @author matthi-g
 */
public class AccountServiceImpl extends DatabaseServiceProxy<Account> implements  AccountService {

    private final AccountRepository accountRepository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the account repository
     */
    public AccountServiceImpl(AccountRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, Account.class, sessionFactory);
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
