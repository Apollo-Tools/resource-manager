package at.uibk.dps.rm.rx.service.database.account;

import at.uibk.dps.rm.entity.dto.account.NewAccountDTO;
import at.uibk.dps.rm.entity.dto.account.RoleEnum;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Role;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.rx.repository.account.AccountRepository;
import at.uibk.dps.rm.rx.repository.account.RoleRepository;
import at.uibk.dps.rm.rx.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.PasswordUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the implementation of the {@link AccountService}.
 *
 * @author matthi-g
 */
public class AccountServiceImpl extends DatabaseServiceProxy<Account> implements AccountService {

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
    public void loginAccount(String username, String password, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<Account> login = withTransactionMaybe(sessionManager -> repository
            .findByUsername(sessionManager, username)
            .switchIfEmpty(Maybe.error(new UnauthorizedException("invalid credentials")))
            .map(account -> {
                if (!account.getIsActive()) {
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
        handleSession(login.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void save(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        NewAccountDTO accountDTO = data.mapTo(NewAccountDTO.class);
        Account newAccount = new Account();
        Maybe<Account> save = withTransactionMaybe(sessionManager -> repository
            .findByUsername(sessionManager, accountDTO.getUsername())
            .flatMap(existingAccount -> Maybe.<Role>error(new AlreadyExistsException(Account.class)))
            .switchIfEmpty(roleRepository.findByRoleName(sessionManager, RoleEnum.DEFAULT.getValue()))
            .switchIfEmpty(Maybe.error(new NotFoundException("default role not found")))
            .flatMapSingle(role -> {
                newAccount.setUsername(accountDTO.getUsername());
                newAccount.setRole(role);
                PasswordUtility passwordUtility = new PasswordUtility();
                char[] password = accountDTO.getPassword().toCharArray();
                String hash = passwordUtility.hashPassword(password);
                newAccount.setPassword(hash);
                return sessionManager.persist(newAccount);
            })
        );
        handleSession(save.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void update(long id, JsonObject fields, Handler<AsyncResult<Void>> resultHandler) {
        Completable update = withTransactionCompletable(sessionManager -> repository
            .findById(sessionManager, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(Account.class)))
            .flatMapCompletable(account -> {
                PasswordUtility passwordUtility = new PasswordUtility();
                char[] oldPassword = fields.getString("old_password").toCharArray();
                char[] newPassword = fields.getString("new_password").toCharArray();
                boolean oldPasswordIsValid = passwordUtility.verifyPassword(account.getPassword(), oldPassword);
                if (!oldPasswordIsValid) {
                    return Completable.error(new UnauthorizedException("old password is invalid"));
                }
                account.setPassword(passwordUtility.hashPassword(newPassword));
                return Completable.complete();
            }));
        handleSession(update, resultHandler);
    }

    @Override
    public void findAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Account>> findAll = withTransactionSingle(repository::findAll);
        handleSession(
            findAll.map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Account account: result) {
                    account.setPassword(null);
                    objects.add(JsonObject.mapFrom(account));
                }
                return new JsonArray(objects);
            })
        , resultHandler);
    }

    @Override
    public void setAccountActive(long accountId, boolean activityLevel, Handler<AsyncResult<Void>> resultHandler) {
        Completable lockAccount = withTransactionCompletable(sessionManager -> sessionManager
            .find(Account.class, accountId)
            .switchIfEmpty(Maybe.error(new NotFoundException(Account.class)))
            .flatMapCompletable(account -> {
                account.setIsActive(activityLevel);
                return Completable.complete();
            }));
        handleSession(lockAccount, resultHandler);
    }
}
