package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.dto.account.NewAccountDTO;
import at.uibk.dps.rm.entity.dto.account.RoleEnum;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Role;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.repository.account.RoleRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import at.uibk.dps.rm.util.misc.PasswordUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

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
            SessionManagerProvider smProvider) {
        super(repository, Account.class, smProvider);
        this.repository = repository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void loginAccount(String username, String password, Handler<AsyncResult<JsonObject>> resultHandler) {
        Single<Account> login = smProvider.withTransactionSingle( sm -> repository
            .findByUsername(sm, username)
            .switchIfEmpty(Single.error(new UnauthorizedException("invalid credentials")))
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
        RxVertxHandler.handleSession(login.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void save(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        NewAccountDTO accountDTO = data.mapTo(NewAccountDTO.class);
        Account newAccount = new Account();
        Single<Account> save = smProvider.withTransactionSingle( sm -> repository
            .findByUsername(sm, accountDTO.getUsername())
            .flatMap(existingAccount -> Maybe.<Role>error(new AlreadyExistsException(Account.class)))
            .switchIfEmpty(roleRepository.findByRoleName(sm, RoleEnum.DEFAULT.getValue()))
            .switchIfEmpty(Single.error(new NotFoundException("default role not found")))
            .flatMap(role -> {
                newAccount.setUsername(accountDTO.getUsername());
                newAccount.setRole(role);
                PasswordUtility passwordUtility = new PasswordUtility();
                char[] password = accountDTO.getPassword().toCharArray();
                String hash = passwordUtility.hashPassword(password);
                newAccount.setPassword(hash);
                return sm.persist(newAccount);
            })
        );
        RxVertxHandler.handleSession(save.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void update(long id, JsonObject fields, Handler<AsyncResult<Void>> resultHandler) {
        Completable update = smProvider.withTransactionCompletable(sm -> repository
            .findByIdAndActive(sm, id)
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
        RxVertxHandler.handleSession(update, resultHandler);
    }

    @Override
    public void setAccountActive(long accountId, boolean activityLevel, Handler<AsyncResult<Void>> resultHandler) {
        Completable lockAccount = smProvider.withTransactionCompletable(sm -> sm
            .find(Account.class, accountId)
            .switchIfEmpty(Maybe.error(new NotFoundException(Account.class)))
            .flatMapCompletable(account -> {
                account.setIsActive(activityLevel);
                return Completable.complete();
            }));
        RxVertxHandler.handleSession(lockAccount, resultHandler);
    }
}
