package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.AccountCredentials;
import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.account.AccountCredentialsRepository;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

import java.util.ArrayList;
import java.util.List;

//TODO: discuss if it is necessary to encrypt secrets / store secrets
/**
 * This is the implementation of the {@link CredentialsService}.
 *
 * @author matthi-g
 */
public class CredentialsServiceImpl extends DatabaseServiceProxy<Credentials> implements CredentialsService {

    private final CredentialsRepository repository;

    private final AccountCredentialsRepository accountCredentialsRepository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the credentials repository
     */
    public CredentialsServiceImpl(CredentialsRepository repository,
            AccountCredentialsRepository accountCredentialsRepository, SessionManagerProvider smProvider) {
        super(repository, Credentials.class, smProvider);
        this.repository = repository;
        this.accountCredentialsRepository = accountCredentialsRepository;
    }

    @Override
    public void findAllByAccountIdAndIncludeExcludeSecrets(long accountId, boolean includeSecrets,
            Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Credentials>> findAll = smProvider.withTransactionSingle(sm -> repository
            .findAllByAccountId(sm, accountId));
        RxVertxHandler.handleSession(
            findAll
                .map(result -> {
                    ArrayList<JsonObject> objects = new ArrayList<>();
                    for (Credentials entity: result) {
                        if (!includeSecrets) {
                            entity.setAccessKey(null);
                            entity.setSecretAccessKey(null);
                            entity.setSessionToken(null);
                        }
                        objects.add(JsonObject.mapFrom(entity));
                    }
                    return new JsonArray(objects);
                }),
            resultHandler
        );
    }

    @Override
    public void saveToAccount(long accountId, JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        Credentials newCredentials = data.mapTo(Credentials.class);
        long providerId = newCredentials.getResourceProvider().getProviderId();
        Single<Credentials> save = smProvider.withTransactionSingle( sm ->
            accountCredentialsRepository.findByAccountAndProvider(sm, accountId, providerId)
                .flatMap(existingCredentials -> Maybe.<ResourceProvider>error(new AlreadyExistsException(Credentials.class)))
                .switchIfEmpty(sm.find(ResourceProvider.class, providerId))
                .switchIfEmpty(Maybe.error(new NotFoundException(ResourceProvider.class)))
                .flatMap(provider -> {
                    newCredentials.setResourceProvider(provider);
                    return sm.find(Account.class, accountId);
                })
                .switchIfEmpty(Single.error(new UnauthorizedException()))
                .flatMap(account -> sm.persist(newCredentials)
                    .map(res -> {
                        AccountCredentials accountCredentials = new AccountCredentials();
                        accountCredentials.setCredentials(newCredentials);
                        accountCredentials.setAccount(account);
                        return accountCredentials;
                    }))
                .flatMap(sm::persist)
                .map(res -> newCredentials)
        );
        RxVertxHandler.handleSession(save.map(JsonObject::mapFrom), resultHandler);
    }
}
