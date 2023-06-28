package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #AccountService.
 *
 * @author matthi-g
 */
public class AccountServiceImpl extends DatabaseServiceProxy<Account> implements  AccountService {

    private final AccountRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the account repository
     */
    public AccountServiceImpl(AccountRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, Account.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<JsonObject> findOneByUsername(String username) {
        CompletionStage<Account> findOne = withSession(session ->
            repository.findByUsername(session, username));
        return Future.fromCompletionStage(findOne)
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<Boolean> existsOneByUsername(String username, boolean isActive) {
        CompletionStage<Account> findOne = withSession(session ->
            repository.findByUsername(session, username));
        return Future.fromCompletionStage(findOne)
            .map(Objects::nonNull);
    }
}
