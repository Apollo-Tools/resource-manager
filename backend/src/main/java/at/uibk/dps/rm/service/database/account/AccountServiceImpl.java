package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.PasswordUtility;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
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

    @Override
    public Future<JsonObject> loginAccount(String username, String password) {
        CompletionStage<Account> login = withSession(session -> repository.findByUsername(session, username)
            .thenApply(account -> {
                if (account == null) {
                    throw new UnauthorizedException("invalid credentials");
                }
                PasswordUtility passwordUtility = new PasswordUtility();
                boolean passwordIsValid = passwordUtility.verifyPassword(account.getPassword(),
                    password.toCharArray());
                if (!passwordIsValid) {
                    throw new UnauthorizedException("invalid credentials");
                }
                return account;
            })
        );
        return transactionToFuture(login).map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonObject> save(JsonObject data) {
        Account newAccount = data.mapTo(Account.class);
        CompletionStage<Account> save = withTransaction(session ->
            repository.findByUsername(session, newAccount.getUsername())
                .thenApply(account -> {
                    ServiceResultValidator.checkExists(account, Account.class);
                    PasswordUtility passwordUtility = new PasswordUtility();
                    char[] password = newAccount.getPassword().toCharArray();
                    String hash = passwordUtility.hashPassword(password);
                    newAccount.setPassword(hash);
                    session.persist(newAccount);
                    return newAccount;
                })
        );
        return transactionToFuture(save).map(result -> {
            JsonObject returnObject = JsonObject.mapFrom(result);
            returnObject.remove("password");
            return returnObject;
        });
    }

    @Override
    public Future<Void> update(long id, JsonObject fields) {
        CompletionStage<Account> update = withTransaction(session -> repository.findById(session, id)
            .thenApply(account -> {
                ServiceResultValidator.checkFound(account, Account.class);
                PasswordUtility passwordUtility = new PasswordUtility();
                char[] oldPassword = fields.getString("old_password").toCharArray();
                char[] newPassword = fields.getString("new_password").toCharArray();
                boolean oldPasswordIsValid = passwordUtility.verifyPassword(account.getPassword(), oldPassword);
                if (!oldPasswordIsValid) {
                    throw new UnauthorizedException("old password is invalid");
                }
                account.setPassword(passwordUtility.hashPassword(newPassword));
                return account;
            }));
        return transactionToFuture(update).mapEmpty();
    }
}
