package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.AccountCredentials;
import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.account.AccountCredentialsRepository;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

//TODO: discuss if it is necessary to encrypt secrets / store secrets
/**
 * This is the implementation of the #CredentialsService.
 *
 * @author matthi-g
 */
public class CredentialsServiceImpl extends DatabaseServiceProxy<Credentials> implements  CredentialsService {

    private final CredentialsRepository repository;

    private final AccountRepository accountRepository;

    private final AccountCredentialsRepository accountCredentialsRepository;

    private final ResourceProviderRepository resourceProviderRepository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the credentials repository
     */
    public CredentialsServiceImpl(CredentialsRepository repository, AccountRepository accountRepository,
            AccountCredentialsRepository accountCredentialsRepository,
            ResourceProviderRepository resourceProviderRepository, Stage.SessionFactory sessionFactory) {
        super(repository, Credentials.class, sessionFactory);
        this.repository = repository;
        this.accountRepository = accountRepository;
        this.accountCredentialsRepository = accountCredentialsRepository;
        this.resourceProviderRepository = resourceProviderRepository;
    }

    @Override
    public Future<JsonArray> findAllByAccountId(long accountId) {
        CompletionStage<List<Credentials>> findAll = withSession(session ->
            repository.findAllByAccountId(session, accountId));
        return Future.fromCompletionStage(findAll)
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Credentials entity: result) {
                    entity.getResourceProvider().setProviderPlatforms(null);
                    entity.getResourceProvider().setEnvironment(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Boolean> existsOneByAccountIdAndProviderId(long accountId, long providerId) {
        CompletionStage<Credentials> findOne = withSession(session ->
            repository.findByAccountIdAndProviderId(session, accountId, providerId));
        return Future.fromCompletionStage(findOne)
            .map(Objects::nonNull);
    }

    @Override
    public Future<JsonObject> saveToAccount(long accountId, JsonObject data) {
        Credentials newCredentials = data.mapTo(Credentials.class);
        long providerId = newCredentials.getResourceProvider().getProviderId();
        CompletionStage<Credentials> save = withTransaction(session ->
            accountCredentialsRepository.findByAccountAndProvider(session, accountId, providerId)
                .thenCompose(accountCredentials -> {
                    ServiceResultValidator.checkExists(accountCredentials, Credentials.class);
                    return resourceProviderRepository.findById(session, providerId);
                })
                .thenCompose(provider -> {
                    ServiceResultValidator.checkFound(provider, ResourceProvider.class);
                    newCredentials.setResourceProvider(provider);
                    return accountRepository.findById(session, accountId);
                })
                .thenCompose(account -> {
                    if (account == null) {
                        throw new UnauthorizedException();
                    }
                    return session.persist(newCredentials)
                        .thenApply(res -> {
                            AccountCredentials accountCredentials = new AccountCredentials();
                            accountCredentials.setCredentials(newCredentials);
                            accountCredentials.setAccount(account);
                            return accountCredentials;
                        });
                })
                .thenCompose(accountCredentials -> session.persist(accountCredentials)
                    .thenApply(res -> newCredentials))
        );
        return transactionToFuture(save).map(credentials -> {
            newCredentials.setResourceProvider(null);
            return JsonObject.mapFrom(credentials);
        });
    }
}
