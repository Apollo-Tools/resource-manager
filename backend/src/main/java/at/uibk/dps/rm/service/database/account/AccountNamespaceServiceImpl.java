package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.repository.account.AccountNamespaceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #AccountNamespaceService.
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
    public Future<JsonObject> saveByAccountIdAndNamespaceId(long accountId, long namespaceId) {
        AccountNamespace accountNamespace = new AccountNamespace();
        CompletionStage<AccountNamespace> create = withTransaction(session ->
            repository.findByAccountIdAndNamespaceId(session, accountId, namespaceId)
                .thenCompose(existing -> {
                    ServiceResultValidator.checkExists(existing, AccountNamespace.class);
                    return session.find(K8sNamespace.class, namespaceId);
                })
                .thenCompose(namespace -> {
                    ServiceResultValidator.checkFound(namespace, K8sNamespace.class);
                    long resourceId = namespace.getResource().getResourceId();
                    accountNamespace.setNamespace(namespace);
                    return repository.findByAccountIdAndResourceId(session, accountId, resourceId);
                })
                .thenCompose(existing -> {
                    ServiceResultValidator.checkExists(existing, "only one namespace per resource allowed");
                    return session.find(Account.class, accountId);
                })
                .thenCompose(account -> {
                    ServiceResultValidator.checkFound(account, Account.class);
                    accountNamespace.setAccount(account);
                    return session.persist(accountNamespace);
                })
                .thenApply(res -> accountNamespace)
        );
        return sessionToFuture(create)
            .map(result -> {
                JsonObject response = new JsonObject();
                response.put("account_id", result.getAccount().getAccountId());
                response.put("namespace_id", result.getNamespace().getNamespaceId());
                return response;
            });
    }

    @Override
    public Future<Void> deleteByAccountIdAndNamespaceId(long accountId, long namespaceId) {
        CompletionStage<Void> delete = withTransaction(session ->
            repository.findByAccountIdAndNamespaceId(session, accountId, namespaceId)
                .thenCompose(resourceEnsemble -> {
                    ServiceResultValidator.checkFound(resourceEnsemble, ResourceEnsemble.class);
                    return session.remove(resourceEnsemble);
                })
        );
        return Future.fromCompletionStage(delete)
            .recover(this::recoverFailure);
    }
}
