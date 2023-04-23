package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Objects;

//TODO: discuss if it is necessary to encrypt secrets / store secrets
/**
 * This is the implementation of the #CredentialsService.
 *
 * @author matthi-g
 */
public class CredentialsServiceImpl extends DatabaseServiceProxy<Credentials> implements  CredentialsService {

    private final CredentialsRepository credentialsRepository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the credentials repository
     */
    public CredentialsServiceImpl(CredentialsRepository repository) {
        super(repository, Credentials.class);
        this.credentialsRepository = repository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(credentialsRepository.findByIdAndFetch(id))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonArray> findAllByAccountId(long accountId) {
        return Future
            .fromCompletionStage(credentialsRepository.findAllByAccountId(accountId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Credentials entity: result) {
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Boolean> existsAtLeastOneByAccount(long accountId) {
        return Future
            .fromCompletionStage(credentialsRepository.findAllByAccountId(accountId))
            .map(result -> result != null && !result.isEmpty());
    }

    @Override
    public Future<Boolean> existsOnyByAccountIdAndProviderId(long accountId, long providerId) {
        return Future
            .fromCompletionStage(credentialsRepository.findByAccountIdAndProviderId(accountId, providerId))
            .map(Objects::nonNull);
    }
}
