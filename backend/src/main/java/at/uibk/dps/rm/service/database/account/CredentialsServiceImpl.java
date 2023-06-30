package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.AccountCredentials;
import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.account.AccountCredentialsRepository;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
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
    public Future<JsonObject> findOne(long id) {
        CompletionStage<Credentials> findOne = withSession(session -> repository.findByIdAndFetch(session, id));
        return Future.fromCompletionStage(findOne)
            .map(result -> {
                if (result != null) {
                    result.getResourceProvider().setProviderPlatforms(null);
                    result.getResourceProvider().setEnvironment(null);
                }
                return JsonObject.mapFrom(result);
            });
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
    public Future<Boolean> existsAtLeastOneByAccount(long accountId) {
        CompletionStage<List<Credentials>> findAll = withSession(session ->
            repository.findAllByAccountId(session, accountId));
        return Future.fromCompletionStage(findAll)
            .map(result -> result != null && !result.isEmpty());
    }

    @Override
    public Future<Boolean> existsOnyByAccountIdAndProviderId(long accountId, long providerId) {
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
                .thenAccept(accountCredentials -> {
                    if (accountCredentials != null) {
                        throw new AlreadyExistsException(AccountCredentials.class);
                    }
                })
                .thenCompose(res -> resourceProviderRepository.findById(session, providerId)
                    .thenAccept(resourceProvider -> {
                        if (resourceProvider == null) {
                            throw new NotFoundException(ResourceProvider.class);
                        }
                        newCredentials.setResourceProvider(resourceProvider);
                    })
                )
                .thenCompose(res -> accountRepository.findById(session, accountId)
                    .thenApply(account -> {
                        if (account == null) {
                            throw new UnauthorizedException();
                        }
                        session.persist(newCredentials);
                        AccountCredentials accountCredentials = new AccountCredentials();
                        accountCredentials.setCredentials(newCredentials);
                        accountCredentials.setAccount(account);
                        session.persist(newCredentials, accountCredentials);
                        return newCredentials;
                    }))
        );
        return Future.fromCompletionStage(save)
            .recover(this::recoverFailure)
            .map(credentials -> {
                newCredentials.setResourceProvider(null);
                return JsonObject.mapFrom(credentials);
            });
    }

    @Override
    public Future<Void> deleteFromAccount(long accountId, long credentialsId) {
        CompletionStage<Void> delete = withTransaction(session ->
            repository.findByIdAndAccountId(session, credentialsId, accountId)
                .thenAccept(entity -> {
                    if (entity == null) {
                        throw new NotFoundException(Credentials.class);
                    }
                    session.remove(entity);
                })
        );
        return Future.fromCompletionStage(delete)
            .recover(this::recoverFailure)
            .mapEmpty();
    }
}
