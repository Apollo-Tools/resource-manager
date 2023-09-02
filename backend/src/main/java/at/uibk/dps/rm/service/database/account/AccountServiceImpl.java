package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.dto.account.NewAccountDTO;
import at.uibk.dps.rm.entity.dto.account.RoleEnum;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.repository.account.RoleRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.PasswordUtility;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #AccountService.
 *
 * @author matthi-g
 */
@Deprecated
public class AccountServiceImpl extends DatabaseServiceProxy<Account> implements  AccountService {

    private final AccountRepository repository;

    private final RoleRepository roleRepository;

    /**
     * Create an instance from the repository and role repository.
     *
     * @param repository the account repository
     * @param roleRepository the role repository
     */
    public AccountServiceImpl(AccountRepository repository, RoleRepository roleRepository,
            Stage.SessionFactory sessionFactory) {
        super(repository, Account.class, sessionFactory);
        this.repository = repository;
        this.roleRepository = roleRepository;
    }

    @Override
    public Future<JsonObject> loginAccount(String username, String password) {
        CompletionStage<Account> login = withSession(session -> repository.findByUsername(session, username)
            .thenApply(account -> {
                if (account == null || !account.getIsActive()) {
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
        return sessionToFuture(login).map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonObject> save(JsonObject data) {
        NewAccountDTO accountDTO = data.mapTo(NewAccountDTO.class);
        Account newAccount = new Account();
        CompletionStage<Account> save = withTransaction(session ->
            repository.findByUsername(session, accountDTO.getUsername())
                .thenCompose(existingAccount -> {
                    ServiceResultValidator.checkExists(existingAccount, Account.class);
                    return roleRepository.findByRoleName(session, RoleEnum.DEFAULT.getValue());
                })
                .thenCompose(role -> {
                    ServiceResultValidator.checkFound(role, "default role not found");
                    newAccount.setUsername(accountDTO.getUsername());
                    newAccount.setRole(role);
                    PasswordUtility passwordUtility = new PasswordUtility();
                    char[] password = accountDTO.getPassword().toCharArray();
                    String hash = passwordUtility.hashPassword(password);
                    newAccount.setPassword(hash);
                    return session.persist(newAccount)
                        .thenApply(res -> newAccount);
                })
        );
        return sessionToFuture(save).map(JsonObject::mapFrom);
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
        return sessionToFuture(update).mapEmpty();
    }

    @Override
    public Future<JsonArray> findAll() {
        CompletionStage<List<Account>> findAll = withSession(repository::findAll);
        return sessionToFuture(findAll)
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Account account: result) {
                    account.setPassword(null);
                    objects.add(JsonObject.mapFrom(account));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Void> setAccountActive(long accountId, boolean activityLevel) {
        CompletionStage<Void> lockAccount = withTransaction(session -> session.find(Account.class, accountId)
            .thenAccept(account -> {
                ServiceResultValidator.checkFound(account, Account.class);
                account.setIsActive(activityLevel);
            }));
        return sessionToFuture(lockAccount);
    }
}
