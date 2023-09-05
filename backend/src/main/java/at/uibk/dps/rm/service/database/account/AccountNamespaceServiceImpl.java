package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.account.AccountNamespaceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;

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
    public AccountNamespaceServiceImpl(AccountNamespaceRepository repository, SessionFactory sessionFactory) {
        super(repository, AccountNamespace.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public void saveByAccountIdAndNamespaceId(long accountId, long namespaceId,
            Handler<AsyncResult<JsonObject>> resultHandler) {
        AccountNamespace accountNamespace = new AccountNamespace();
        Maybe<AccountNamespace> create = withTransactionMaybe(sessionManager ->
            repository.findByAccountIdAndNamespaceId(sessionManager, accountId, namespaceId)
                .flatMap(existingService -> Maybe.<K8sNamespace>error(new AlreadyExistsException(AccountNamespace.class)))
                .switchIfEmpty(sessionManager.find(K8sNamespace.class, namespaceId))
                .switchIfEmpty(Maybe.error(new NotFoundException(K8sNamespace.class)))
                .flatMapSingle(namespace -> {
                    long resourceId = namespace.getResource().getResourceId();
                    accountNamespace.setNamespace(namespace);
                    return repository.findByAccountIdAndResourceId(sessionManager, accountId, resourceId);
                })
                .flatMap(existing -> {
                    if (!existing.isEmpty()) {
                        return Maybe.error(new AlreadyExistsException("only one namespace per resource allowed"));
                    }
                    return sessionManager.find(Account.class, accountId);
                })
                .switchIfEmpty(Maybe.error(new NotFoundException(Account.class)))
                .flatMapSingle(account -> {
                    accountNamespace.setAccount(account);
                    return sessionManager.persist(accountNamespace);
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
        Completable delete = withTransactionCompletable(sessionManager -> repository
            .findByAccountIdAndNamespaceId(sessionManager, accountId, namespaceId)
            .switchIfEmpty(Maybe.error(new NotFoundException(AccountNamespace.class)))
            .flatMapCompletable(sessionManager::remove)
        );
        RxVertxHandler.handleSession(delete, resultHandler);
    }
}
