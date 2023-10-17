package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.account.AccountNamespaceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * This is the implementation of the {@link AccountNamespaceService}.
 *
 * @author matthi-g
 */
public class AccountNamespaceServiceImpl extends DatabaseServiceProxy<AccountNamespace> implements
    AccountNamespaceService {

    private final AccountNamespaceRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the resource ensemble repository     *
     */
    public AccountNamespaceServiceImpl(AccountNamespaceRepository repository, SessionManagerProvider smProvider) {
        super(repository, AccountNamespace.class, smProvider);
        this.repository = repository;
    }

    @Override
    public void saveByAccountIdAndNamespaceId(long accountId, long namespaceId,
            Handler<AsyncResult<JsonObject>> resultHandler) {
        AccountNamespace accountNamespace = new AccountNamespace();
        Single<AccountNamespace> create = smProvider.withTransactionSingle(sm ->
            repository.findByAccountIdAndNamespaceId(sm, accountId, namespaceId)
                .flatMap(existingService -> Maybe.<K8sNamespace>error(new AlreadyExistsException(AccountNamespace.class)))
                .switchIfEmpty(sm.find(K8sNamespace.class, namespaceId))
                .switchIfEmpty(Maybe.error(new NotFoundException(K8sNamespace.class)))
                .flatMap(namespace -> {
                    long resourceId = namespace.getResource().getResourceId();
                    accountNamespace.setNamespace(namespace);
                    return repository.findByAccountIdAndResourceId(sm, accountId, resourceId);
                })
                .flatMap(existingNamespace -> Maybe.<Account>error(new AlreadyExistsException("only one namespace " +
                    "per resource allowed")))
                .switchIfEmpty(sm.find(Account.class, accountId))
                .switchIfEmpty(Single.error(new NotFoundException(Account.class)))
                .flatMap(account -> {
                    accountNamespace.setAccount(account);
                    return sm.persist(accountNamespace);
                })
        );
        RxVertxHandler.handleSession(create
            .map(result -> {
                JsonObject response = new JsonObject();
                response.put("account_id", result.getAccount().getAccountId());
                response.put("namespace_id", result.getNamespace().getNamespaceId());
                return response;
            }),
            resultHandler);
    }

    @Override
    public void deleteByAccountIdAndNamespaceId(long accountId, long namespaceId,
            Handler<AsyncResult<Void>> resultHandler) {
        Completable delete = smProvider.withTransactionCompletable(sm -> repository
            .findByAccountIdAndNamespaceId(sm, accountId, namespaceId)
            .switchIfEmpty(Maybe.error(new NotFoundException(AccountNamespace.class)))
            .flatMapCompletable(sm::remove)
        );
        RxVertxHandler.handleSession(delete, resultHandler);
    }
}
