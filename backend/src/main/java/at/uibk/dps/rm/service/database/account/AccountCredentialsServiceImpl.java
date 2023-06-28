package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.AccountCredentials;
import at.uibk.dps.rm.repository.account.AccountCredentialsRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #AccountCredentialsService.
 *
 * @author matthi-g
 */
public class AccountCredentialsServiceImpl extends DatabaseServiceProxy<AccountCredentials> implements  AccountCredentialsService {

    private final AccountCredentialsRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the account credentials repository
     */
    public AccountCredentialsServiceImpl(AccountCredentialsRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, AccountCredentials.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<JsonObject> findOneByCredentialsAndAccount(long credentialsId, long accountId) {
        CompletionStage<AccountCredentials> findOne = withSession(session ->
            repository.findByCredentialsAndAccount(session, credentialsId, accountId));
        return Future.fromCompletionStage(findOne)
            .map(result -> {
                if (result != null) {
                    result.getCredentials().setResourceProvider(null);
                }
                return JsonObject.mapFrom(result);
            });
    }

    @Override
    public Future<Boolean> existsOneByAccountAndProvider(long accountId, long providerId) {
        CompletionStage<AccountCredentials> findOne = withSession(session ->
            repository.findByAccountAndProvider(session, accountId, providerId));
        return Future.fromCompletionStage(findOne)
            .map(Objects::nonNull);
    }
}
