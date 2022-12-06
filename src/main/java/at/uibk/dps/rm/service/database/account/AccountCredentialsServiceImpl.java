package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.AccountCredentials;
import at.uibk.dps.rm.repository.AccountCredentialsRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

public class AccountCredentialsServiceImpl extends ServiceProxy<AccountCredentials> implements  AccountCredentialsService {

    private final AccountCredentialsRepository accountCredentialsRepository;

    public AccountCredentialsServiceImpl(AccountCredentialsRepository repository) {
        super(repository, AccountCredentials.class);
        this.accountCredentialsRepository = repository;
    }

    @Override
    public Future<JsonObject> findOneByCredentials(long credentialsId) {
        return Future
            .fromCompletionStage(accountCredentialsRepository.findByCredentials(credentialsId))
            .map(result -> {
                if (result != null) {
                    result.getCredentials().setResourceProvider(null);
                    result.setAccount(null);
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

    @Override
    public Future<Boolean> existsOneByCredentials(long credentialsId) {
        return Future
            .fromCompletionStage(accountCredentialsRepository.findByCredentials(credentialsId))
            .map(Objects::nonNull);
    }
}
