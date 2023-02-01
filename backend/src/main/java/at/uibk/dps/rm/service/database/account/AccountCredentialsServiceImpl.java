package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.AccountCredentials;
import at.uibk.dps.rm.repository.account.AccountCredentialsRepository;
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
