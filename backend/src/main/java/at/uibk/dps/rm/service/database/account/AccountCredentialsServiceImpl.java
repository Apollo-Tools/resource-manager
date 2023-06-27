package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.AccountCredentials;
import at.uibk.dps.rm.repository.account.AccountCredentialsRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.Objects;

/**
 * This is the implementation of the #AccountCredentialsService.
 *
 * @author matthi-g
 */
public class AccountCredentialsServiceImpl extends DatabaseServiceProxy<AccountCredentials> implements  AccountCredentialsService {

    private final AccountCredentialsRepository accountCredentialsRepository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the account credentials repository
     */
    public AccountCredentialsServiceImpl(AccountCredentialsRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, AccountCredentials.class, sessionFactory);
        this.accountCredentialsRepository = repository;
    }

    @Override
    public Future<JsonObject> findOneByCredentialsAndAccount(long credentialsId, long accountId) {
        return Future
            .fromCompletionStage(accountCredentialsRepository.findByCredentialsAndAccount(credentialsId, accountId))
            .map(result -> {
                if (result != null) {
                    result.getCredentials().setResourceProvider(null);
                }
                return JsonObject.mapFrom(result);
            });
    }

    @Override
    public Future<Boolean> existsOneByAccountAndProvider(long accountId, long providerId) {
        return Future
            .fromCompletionStage(accountCredentialsRepository.findByAccountAndProvider(accountId, providerId))
            .map(Objects::nonNull);
    }
}
